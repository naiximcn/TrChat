package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.internal.data.Users
import org.bukkit.entity.Player
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