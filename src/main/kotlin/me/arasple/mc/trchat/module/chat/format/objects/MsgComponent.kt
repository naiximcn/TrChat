package me.arasple.mc.trchat.module.chat.format.objects

import me.arasple.mc.trchat.TrChatFiles.function
import me.arasple.mc.trchat.module.data.Cooldowns
import me.arasple.mc.trchat.module.data.Users
import me.arasple.mc.trchat.module.func.ChatFunctions
import me.arasple.mc.trchat.util.*
import org.apache.commons.lang.math.NumberUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.util.VariableReader
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.module.lang.sendLang
import taboolib.module.nms.getI18nName
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.hoverItem
import taboolib.platform.util.isNotAir

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
class MsgComponent : JsonComponent {

    var isPrivateChat = false
    private var defualtColor: ChatColor? = null

    constructor(text: String?, hover: List<String?>?, suggest: String?, command: String?, url: String?) : super(text, hover, suggest, command, url)

    constructor(partSection: LinkedHashMap<*, *>) : super(partSection) {
        defualtColor = ChatColor.getByChar(partSection["default-color"].toString())
    }

    fun toMsgTellraw(player: Player, m: String): TellrawJson {
        var message = m
        message = MessageColors.replaceWithPermission(player, message)

        val tellraw = TellrawJson()
        ChatFunctions.functions.filter { f -> checkCondition(player, f.requirement) }.forEach { function ->
            message = replacePattern(message, function.pattern, function.filterTextPattern, "<" + function.name + ":{0}>")
        }
        // At & Item Show
        val atEnabled = Users.getCooldownLeft(player.uniqueId, Cooldowns.CooldownType.MENTION) <= 0
        val atFormat = function.getString("GENERAL.MENTION.FORMAT").colored()
        if (atEnabled) {
            for (p in Players.getPlayers()) {
                if (!function.getBoolean("GENERAL.MENTION.SELF-MENTION", false) && p.equals(player.name, ignoreCase = true)) {
                    continue
                }
                message = message.replace("(?i)(@)?" + p.toRegex(), "<AT:$p>")
            }
        }
        val itemDisplayEnabled = function.getBoolean("GENERAL.ITEM-SHOW.ENABLE", true)
        val itemKeys = function.getStringList("GENERAL.ITEM-SHOW.KEYS")
        val itemFormat = function.getString("GENERAL.ITEM-SHOW.FORMAT", "§8[§3{0} §bx{1}§8]").colored()
        if (itemDisplayEnabled) {
            for (key in itemKeys) {
                for (i in 0..8) {
                    message = message.replace("$key-$i", "<ITEM:$i>")
                }
                message = message.replace(key, "<ITEM:" + player.inventory.heldItemSlot + ">")
            }
        }

        // Custom Functions
        for (v in VariableReader(message, '<', '>').parts) {
            if (v.isVariable) {
                val args = v.text.split(":".toRegex(), 2)
                if (itemDisplayEnabled && args[0].equals("ITEM", ignoreCase = true)) {
                    val slot = NumberUtils.toInt(args[1], player.inventory.heldItemSlot)
                    val item = player.inventory.getItem(slot) ?: ItemStack(Material.AIR)
                    tellraw.append(Users.itemCache.computeIfAbsent(item) {
                        TellrawJson()
                            .append(itemFormat.replaceWithOrder(getName(item), if (item.isNotAir()) item.amount else 1) + defualtColor)
                            .hoverItem(item)
                    })
                    continue
                }
                if (atEnabled && "AT".equals(args[0], ignoreCase = true) && !isPrivateChat) {
                    val atPlayer = args[1]
                    tellraw.append(atFormat.replaceWithOrder(atPlayer) + defualtColor)
                    if (function.getBoolean("GENERAL.MENTION.NOTIFY") && Bukkit.getPlayerExact(atPlayer) != null && Bukkit.getPlayerExact(atPlayer)!!.isOnline) {
                        getProxyPlayer(atPlayer)!!.sendLang("MENTIONS@NOTIFY", player.name)
                    }
                    Users.updateCooldown(player.uniqueId, Cooldowns.CooldownType.MENTION, function.getLong("GENERAL.MENTION.COOLDOWNS"))
                    continue
                }
                val function = ChatFunctions.mathFunction(args[0])
                if (function != null) {
                    tellraw.append(function.displayJson.toTellrawJson(player, true, args[1]))
                    continue
                }
            }
            tellraw.append(toTellrawPart(player, defualtColor.toString() + v.text, message))
        }
        return tellraw
    }

    fun toTellrawPart(player: Player, text: String?, message: String?): TellrawJson {
        val tellraw = TellrawJson()
        tellraw.append((text ?: "§8[§fNull§8]").replace("\$message", message!!))
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
        return tellraw
    }

    private fun getName(item: ItemStack): String {
        return item.getI18nName(null)
    }
}