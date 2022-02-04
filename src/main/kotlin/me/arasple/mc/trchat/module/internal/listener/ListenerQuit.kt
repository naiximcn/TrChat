package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.module.display.ChatSession
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.expansion.releaseDataContainer

/**
 * @author wlys
 * @since 2021/12/11 23:19
 */
object ListenerQuit {

    @SubscribeEvent(EventPriority.HIGHEST)
    fun e(e: PlayerQuitEvent) {
        Functions.itemShowDelay.get().reset(e.player.name)
        Functions.mentionDelay.get().reset(e.player.name)
        ChatSession.removeSession(e.player)
        e.player.releaseDataContainer()
    }
}