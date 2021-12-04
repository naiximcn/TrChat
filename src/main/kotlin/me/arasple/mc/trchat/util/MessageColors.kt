package me.arasple.mc.trchat.util

import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide

/**
 * @author Arasple
 * @date 2019/8/15 20:52
 */
@PlatformSide([Platform.BUKKIT])
object MessageColors {

    private val COLOR_CODES = ChatColor.ALL_CODES.map { it }

    private const val COLOR_CHAR = ChatColor.COLOR_CHAR.toString()
    private const val COLOR_PERMISSION_NODE = "trchat.color."
    private const val FORCE_CHAT_COLOR_PERMISSION_NODE = "trchat.color.force-defaultcolor."

    fun replaceWithPermission(player: Player?, strings: List<String>): List<String> {
        return if (player == null) strings else strings.map { replaceWithPermission(player, it) }
    }

    fun replaceWithPermission(player: Player?, s: String): String {
        var string = s

        player ?: return string

        if (player.hasPermission("$COLOR_PERMISSION_NODE*")) {
            string = string.coloredAll()
        } else {
            for (code in COLOR_CODES) {
                if (player.hasPermission(COLOR_PERMISSION_NODE + code)) {
                    string = string.replace("&$code", COLOR_CHAR + code)
                }
            }
        }
        if (player.hasPermission(COLOR_PERMISSION_NODE + "hex")) {
            string = HexUtils.parseHex(string)
        }
        if (player.hasPermission(COLOR_PERMISSION_NODE + "rainbow")) {
            string = HexUtils.parseRainbow(string)
        }
        if (player.hasPermission(COLOR_PERMISSION_NODE + "gradients")) {
            string = HexUtils.parseGradients(string)
        }

        return string
    }

    fun catchDefaultMessageColor(player: Player, defaultColor: String?): String? {
        if (player.hasPermission("$FORCE_CHAT_COLOR_PERMISSION_NODE*")) {
            return defaultColor
        }

        for (code in COLOR_CODES) {
            if (player.hasPermission(FORCE_CHAT_COLOR_PERMISSION_NODE + code)) {
                return COLOR_CHAR + code
            }
        }

        return defaultColor
    }
}