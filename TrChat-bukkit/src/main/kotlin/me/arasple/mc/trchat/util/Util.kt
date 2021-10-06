package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.internal.data.Users
import org.bukkit.entity.Player
import taboolib.module.chat.colored
import taboolib.platform.util.sendLang

/**
 * Util
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/9/12 18:11
 */
fun Player.checkMute(): Boolean {
    // Global mute
    if (TrChat.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
        sendLang("General-Global-Muting")
        return false
    }
    // Mute
    if (Users.isMuted(this)) {
        sendLang("General-Muted")
        return false
    }
    return true
}

internal fun String.filterUUID(): String =
    replace("cmd=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex(), "")
        .replace("chat=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex(), "")

fun String.coloredAll(): String = HexUtils.colorify(colored())