package me.arasple.mc.trchat.common.chat.format.objects

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.TrChatFiles.function
import me.arasple.mc.trchat.common.function.ChatFunctions
import me.arasple.mc.trchat.internal.data.Cooldowns
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.proxy.Proxy
import me.arasple.mc.trchat.internal.proxy.bukkit.Players
import me.arasple.mc.trchat.internal.script.Condition
import me.arasple.mc.trchat.util.MessageColors
import me.arasple.mc.trchat.util.replacePattern
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.util.VariableReader
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.nms.getI18nName
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem
import taboolib.platform.util.hoverItem
import taboolib.platform.util.isAir
import taboolib.platform.util.sendLang

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
class MsgComponent : JsonComponent {

    private var defaultColor: ChatColor? = null

    constructor(text: String?, hover: List<String?>?, suggest: String?, command: String?, url: String?, copy: String?) : super(text, hover, suggest, command, url, copy)

    constructor(partSection: LinkedHashMap<*, *>) : super(partSection) {
        defaultColor = ChatColor.getByChar(partSection["default-color"].toString())
    }

    fun toMsgTellraw(player: Player, msg: String, isPrivateChat: Boolean): TellrawJson {
        val defaultColor = MessageColors.catchDefaultMessageColor(player, defaultColor)
        var message = defaultColor.toString() + msg
        message = MessageColors.replaceWithPermission(player, message)

        val tellraw = TellrawJson()
        // Custom Functions
        ChatFunctions.functions.filter { f -> Condition.eval(player, f.requirement).asBoolean() }.forEach { function ->
            message = replacePattern(message, function.pattern, function.filterTextPattern, "<" + function.name + ":{0}>")
        }
        // At & Item Show
        val atEnabled = function.getBoolean("GENERAL.MENTION.ENABLE", true) && !Users.isInCooldown(player.uniqueId, Cooldowns.CooldownType.MENTION)
        val atFormat = function.getString("GENERAL.MENTION.FORMAT").colored()
        if (atEnabled) {
            for (p in Players.getPlayers()) {
                if (!function.getBoolean("GENERAL.MENTION.SELF-MENTION", false) && p.equals(player.name, ignoreCase = true)) {
                    continue
                }
                message = message.replace("(?i)(@)?$p".toRegex(), "<AT:$p>")
            }
        }
        val itemDisplayEnabled = function.getBoolean("GENERAL.ITEM-SHOW.ENABLE", true)
        val itemKeys = function.getStringList("GENERAL.ITEM-SHOW.KEYS")
        val itemFormat = function.getString("GENERAL.ITEM-SHOW.FORMAT", "&8[&3{0} &bx{1}&8]").colored()
        if (itemDisplayEnabled) {
            for (key in itemKeys) {
                for (i in 0..8) {
                    message = message.replace("$key-$i", "<ITEM:$i>", ignoreCase = true)
                }
                message = message.replace(key, "<ITEM:" + player.inventory.heldItemSlot + ">", ignoreCase = true)
            }
        }

        for (v in VariableReader(message, '<', '>').parts) {
            if (v.isVariable) {
                val args = v.text.split(':', limit = 2)
                // Item Show
                if (itemDisplayEnabled && args[0] == "ITEM") {
                    val slot = args[1].toIntOrNull() ?: player.inventory.heldItemSlot
                    val item = player.inventory.getItem(slot)
                    if (item.isAir()) {
                        player.sendLang("General-Cant-Show-Air")
                        continue
                    }
                    tellraw.append(Users.itemCache.computeIfAbsent(item!!) {
                        TellrawJson()
                            .append(itemFormat.replaceWithOrder(item.getName(player), item.amount.toString() + defaultColor))
                            .hoverItem(item.clone())
                    })
                    continue
                }
                // At
                if (atEnabled && args[0] == "AT" && !isPrivateChat) {
                    val atPlayer = args[1]
                    tellraw.append(atFormat.replaceWithOrder(atPlayer) + defaultColor)
                    if (function.getBoolean("GENERAL.MENTION.NOTIFY")) {
                        Proxy.sendProxyLang(player, atPlayer, "Mentions-Notify", player.name)
                    }
                    Users.updateCooldown(player.uniqueId, Cooldowns.CooldownType.MENTION, function.getLong("GENERAL.MENTION.COOLDOWNS"))
                    continue
                }
                // Custom Functions
                val function = ChatFunctions.matchFunction(args[0])
                if (function != null) {
                    tellraw.append(function.displayJson.toTellrawJson(player, true, args[1]))
                    function.run?.let {
                        TrChatAPI.instantKether(player, it)
                    }
                    continue
                }
            }
            tellraw.append(toTellrawPart(player, defaultColor.toString() + v.text.filter(), message))
        }
        return tellraw
    }

    private fun toTellrawPart(player: Player, text: String?, message: String?): TellrawJson {
        val tellraw = TellrawJson()
        tellraw.append((text ?: "&8[&fNull&8]".colored()).replace("\$message", message!!))
        if (hover != null) {
            tellraw.hoverText(hover!!.replacePlaceholder(player).replace("\$message", message))
        }
        if (suggest != null) {
            tellraw.suggestCommand(suggest!!.replacePlaceholder(player).replace("\$message", message))
        }
        if (command != null) {
            tellraw.runCommand(command!!.replacePlaceholder(player).replace("\$message", message))
        }
        if (url != null) {
            tellraw.openURL(url!!.replacePlaceholder(player).replace("\$message", message))
        }
        if (copy != null) {
            tellraw.copyToClipboard(copy!!.replacePlaceholder(player).replace("\$message", message))
        }
        return tellraw
    }

    private fun ItemStack.getName(player: Player): String {
        return if ((function.getBoolean("GENERAL.ITEM-SHOW.ORIGIN-NAME", false)
                || itemMeta == null) || !itemMeta!!.hasDisplayName()
        ) {
            getI18nName(player)
        } else {
            itemMeta!!.displayName
        }
    }

    private fun String.filter(): String =
        replace("cmd=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex(), "")
            .replace("chat=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex(), "")
}