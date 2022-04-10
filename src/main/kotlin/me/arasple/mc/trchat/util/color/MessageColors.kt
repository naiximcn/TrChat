package me.arasple.mc.trchat.util.color

import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.module.chat.colored

/**
 * @author Arasple
 * @date 2019/8/15 20:52
 */
@PlatformSide([Platform.BUKKIT])
object MessageColors {

    private val COLOR_CODES = ChatColor.ALL_CODES.map { it.toString() }

    private const val COLOR_CHAR = ChatColor.COLOR_CHAR.toString()
    private const val COLOR_PERMISSION_NODE = "trchat.color."
    private const val FORCE_CHAT_COLOR_PERMISSION_NODE = "trchat.color.force-defaultcolor."

    fun replaceWithPermission(player: Player, strings: List<String>): List<String> {
        return strings.map { replaceWithPermission(player, it) }
    }

    fun replaceWithPermission(player: Player, s: String): String {
        var string = s

        if (player.hasPermission("$COLOR_PERMISSION_NODE*")) {
            string = string.colored()
        } else {
            for (code in COLOR_CODES) {
                if (player.hasPermission(COLOR_PERMISSION_NODE + code)) {
                    string = string.replace("&$code", COLOR_CHAR + code)
                }
            }
        }

        string = if (player.hasPermission(COLOR_PERMISSION_NODE + "rainbow")) {
            string.parseRainbow()
        } else {
            string.replace(Hex.RAINBOW_PATTERN.toRegex(), "")
        }

        string = if (player.hasPermission(COLOR_PERMISSION_NODE + "gradients")) {
            string.parseGradients()
        } else {
            string.replace(Hex.GRADIENT_PATTERN.toRegex(), "")
        }

        if (player.hasPermission(COLOR_PERMISSION_NODE + "hex")) {
            string = string.parseHex()
        } else {
            Hex.HEX_PATTERNS.forEach { string = string.replace(it.toRegex(), "") }
        }

        return string
    }

    fun defaultColored(color: DefaultColor, player: Player, msg: String): String {
        var message = msg

        message = replaceWithPermission(player, message)

        message = when (color.type) {
            DefaultColor.ColorType.NORMAL -> color.color + message
            DefaultColor.ColorType.SPECIAL -> (color.color + message).parseRainbow().parseGradients()
        }

        return message
    }

    fun catchDefaultMessageColor(player: Player, defaultColor: DefaultColor): DefaultColor {
        if (player.hasPermission("$FORCE_CHAT_COLOR_PERMISSION_NODE*")) {
            return defaultColor
        }

        for (code in COLOR_CODES) {
            if (player.hasPermission(FORCE_CHAT_COLOR_PERMISSION_NODE + code)) {
                return DefaultColor(code)
            }
        }

        return defaultColor
    }
}