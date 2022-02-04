package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.script.Condition
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * Util
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/9/12 18:11
 */
fun String.toCondition() = Condition(this)

fun Player.getSession() = ChatSession.getSession(this)

fun Player.checkMute(): Boolean {
    if (TrChat.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
        sendLang("General-Global-Muting")
        return false
    }
    if (this.getSession().isMuted) {
        sendLang("General-Muted")
        return false
    }
    return true
}

fun notify(notify: Array<out ProxyCommandSender>, node: String, vararg args: Any) {
    notify.forEach {
        it.sendLang(node, *args)
    }
}