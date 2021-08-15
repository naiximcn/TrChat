package me.arasple.mc.trchat.util

import org.bukkit.ChatColor
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

    val COLOR_CODES = ChatColor.values().map { it.char }

    private const val COLOR_CHAR = ChatColor.COLOR_CHAR.toString()
    private const val COLOR_PERMISSION_NODE = "trchat.color."
    private const val FORCE_CHAT_COLOR_PERMISSION_NODE = "trchat.color.force-defaultcolor."

    fun replaceWithPermission(player: Player?, strings: List<String>): List<String> {
        return if (player == null) strings else strings.map { replaceWithPermission(player, it) }
    }

    fun replaceWithPermission(player: Player?, s: String): String {
        var string = s
        if (player == null) {
            return string
        }

        if (player.hasPermission("$COLOR_PERMISSION_NODE*")) {
            string = string.colored()
        } else {
            for (code in COLOR_CODES) {
                if (player.hasPermission(COLOR_PERMISSION_NODE + code)) {
                    string = string.replace("&$code", COLOR_CHAR + code)
                }
            }
        }

        return string
    }

    fun catchDefaultMessageColor(player: Player, defaultColor: ChatColor?): ChatColor? {
        if (player.hasPermission("$FORCE_CHAT_COLOR_PERMISSION_NODE*")) {
            return defaultColor
        }

        for (code in COLOR_CODES) {
            if (player.hasPermission(FORCE_CHAT_COLOR_PERMISSION_NODE + code)) {
                return ChatColor.getByChar(code)
            }
        }

        return defaultColor
    }
}