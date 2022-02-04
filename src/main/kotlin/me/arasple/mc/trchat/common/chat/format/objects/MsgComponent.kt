package me.arasple.mc.trchat.common.chat.format.objects

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.TrChatFiles.function
import me.arasple.mc.trchat.common.function.ChatFunctions
import me.arasple.mc.trchat.internal.data.Cooldowns
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.util.proxy.Proxy
import me.arasple.mc.trchat.util.proxy.bukkit.Players
import me.arasple.mc.trchat.module.display.format.JsonComponent
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.coloredAll
import me.arasple.mc.trchat.util.replacePattern
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.util.VariableReader
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.getI18nName
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
class MsgComponent : JsonComponent {

    private var defaultColor: String? = null

    constructor(text: String?, hover: List<String?>?, suggest: String?, command: String?, url: String?, copy: String?) : super(text, hover, suggest, command, url, copy)

    constructor(partSection: LinkedHashMap<*, *>) : super(partSection) {
        defaultColor = partSection["default-color"].toString().ifEmpty { null }
    }

    fun toMsgTellraw(player: Player, msg: String, isPrivateChat: Boolean): TellrawJson {
        val defaultColor = MessageColors.catchDefaultMessageColor(player, defaultColor)
        var message = msg.defaultColored(defaultColor)
        message = MessageColors.replaceWithPermission(player, message)

        val tellraw = TellrawJson()
        // Custom Functions
        ChatFunctions.functions.filter { f -> Condition.eval(player, f.requirement).asBoolean() }.forEach { function ->
            message = replacePattern(message, function.pattern, function.filterTextPattern, "<<" + function.name + ":{0}>>")
        }
        // At
        val atEnabled = function.getBoolean("GENERAL.MENTION.ENABLE", true) && !Users.isInCooldown(player.uniqueId, Cooldowns.CooldownType.MENTION)
        val atFormat = function.getString("GENERAL.MENTION.FORMAT")!!.coloredAll()
        if (atEnabled) {
            for (p in Players.getPlayers()) {
                if (!function.getBoolean("GENERAL.MENTION.SELF-MENTION", false) && p.equals(player.name, ignoreCase = true)) {
                    continue
                }
                message = message.replace("(?i)(@)?$p".toRegex(), "<<AT:$p>>")
            }
        }
        // Item Show
        val itemDisplayEnabled = function.getBoolean("GENERAL.ITEM-SHOW.ENABLE", true)
        val itemKeys = function.getStringList("GENERAL.ITEM-SHOW.KEYS")
        val itemFormat = function.getString("GENERAL.ITEM-SHOW.FORMAT", "&8[&3{0} &bx{1}&8]")!!.coloredAll()
        if (itemDisplayEnabled) {
            for (key in itemKeys) {
                for (i in 0..8) {
                    message = message.replace("$key-$i", "<<ITEM:$i>>", ignoreCase = true)
                }
                message = message.replace(key, "<<ITEM:" + player.inventory.heldItemSlot + ">>", ignoreCase = true)
            }
        }

        for (v in VariableReader(message, '<', '>', repeat = 2).parts) {
            if (v.isVariable) {
                val args = v.text.split(':', limit = 2)
                // Item Show
                if (itemDisplayEnabled && args[0] == "ITEM") {
                    val slot = args[1].toIntOrNull() ?: player.inventory.heldItemSlot
                    val item = player.inventory.getItem(slot) ?: ItemStack(Material.AIR)
                    tellraw.append(Users.itemCache.computeIfAbsent(item) {
                        TellrawJson()
                            .append(itemFormat.replaceWithOrder(item.getName(player), item.amount.toString()))
                            .hoverItemFixed(item.run {
                                if (function.getBoolean("GENERAL.ITEM-SHOW.COMPATIBLE", false)) {
                                    buildItem(this) { material = Material.STONE }
                                } else {
                                    this
                                }
                            })
                    })
                    continue
                }
                // At
                if (atEnabled && args[0] == "AT" && !isPrivateChat) {
                    val atPlayer = args[1]
                    tellraw.append(atFormat.replaceWithOrder(atPlayer))
                    if (function.getBoolean("GENERAL.MENTION.NOTIFY")) {
                        Proxy.sendProxyLang(player, atPlayer, "Mentions-Notify", player.name)
                    }
                    Users.updateCooldown(player.uniqueId, Cooldowns.CooldownType.MENTION, function.getLong("GENERAL.MENTION.COOLDOWNS"))
                    continue
                }
                // Custom Functions
                val function = ChatFunctions.matchFunction(args[0])
                if (function != null) {
                    tellraw.append(function.displayJson.toTellrawJson(player, args[1], function = true))
                    function.run?.let { TrChatAPI.instantKether(player, it) }
                    continue
                }
            }
            tellraw.append(toTellrawPart(player, v.text.defaultColored(defaultColor), message))
        }
        return tellraw
    }

    private fun toTellrawPart(player: Player, text: String?, message: String): TellrawJson {
        val tellraw = TellrawJson()
        tellraw.append((text ?: "§8[§fNull§8]").replace("\$message", message))
        hover?.let { tellraw.hoverText(it.replacePlaceholder(player).replace("\$message", message).coloredAll()) }
        suggest?.let { tellraw.suggestCommand(it.replacePlaceholder(player).replace("\$message", message)) }
        command?.let { tellraw.runCommand(it.replacePlaceholder(player).replace("\$message", message)) }
        url?.let { tellraw.openURL(it.replacePlaceholder(player).replace("\$message", message)) }
        copy?.let { tellraw.copyOrSuggest(it.replacePlaceholder(player).replace("\$message", message)) }
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

    private fun String.defaultColored(defaultColor: String?): String {
        return defaultColor?.let {
            when {
                it.length == 1 -> ChatColor.getByChar(it[0]).toString() + this
                it.startsWith('#') -> ChatColor.of(it).toString() + this
                else -> (it + this).coloredAll()
            }
        } ?: this
    }
}