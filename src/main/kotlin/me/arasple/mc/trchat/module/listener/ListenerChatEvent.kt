package me.arasple.mc.trchat.module.listener

import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.util.checkMute
import me.arasple.mc.trchat.util.getSession
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.Strings
import taboolib.platform.util.sendLang

/**
 * @author Arasple, wlys
 * @date 2019/11/30 12:10
 */
@PlatformSide([Platform.BUKKIT])
object ListenerChatEvent {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        e.isCancelled = true
        val player = e.player

        player.getSession().recipients = e.recipients

        if (!player.checkMute()) {
            return
        }

        if (!checkLimits(player, e.message)) {
            return
        }

        e.handlers.registeredListeners.forEach {
            if (it.plugin.isEnabled && it.priority == org.bukkit.event.EventPriority.MONITOR) {
                try {
                    it.callEvent(AsyncPlayerChatEvent(e.isAsynchronous, e.player, e.message, e.recipients))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkLimits(p: Player, message: String): Boolean {
        if (p.hasPermission("trchat.bypass.*")) {
            return true
        }
        if (!p.hasPermission("trchat.bypass.chatlength")) {
            if (message.length > Settings.chatLengthLimit) {
                p.sendLang("General-Too-Long", message.length, Settings.chatLengthLimit)
                return false
            }
        }
        if (!p.hasPermission("trchat.bypass.repeat")) {
            val lastMessage = p.getSession().lastMessage
            if (Strings.similarDegree(lastMessage, message) > Settings.chatSimilarity) {
                p.sendLang("General-Too-Similar")
                return false
            } else {
                p.getSession().lastMessage = message
            }
        }
        if (!p.hasPermission("trchat.bypass.chatcd")) {
            if (!Settings.chatDelay.get().hasNext(p.name)) {
                p.sendLang("Cooldowns-Chat", Settings.CONF.getDouble("Chat.Cooldown").toString())
                return false
            }
        }
        if (!p.hasPermission("trchat.bypass.itemcd")) {
            if (Functions.itemShow.getStringList("Keys").any { message.contains(it) }) {
                if (!Functions.itemShowDelay.get().hasNext(p.name)) {
                    p.sendLang("Cooldowns-Item-Show", Functions.itemShow.getDouble("Cooldowns").toString())
                    return false
                }
            }
        }
        return true
    }
}