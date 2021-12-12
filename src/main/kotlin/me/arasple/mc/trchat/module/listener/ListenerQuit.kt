package me.arasple.mc.trchat.module.listener

import me.arasple.mc.trchat.module.display.ChatSession
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.expansion.releaseDataContainer

/**
 * @author wlys
 * @since 2021/12/11 23:19
 */
object ListenerQuit {

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        ChatSession.removeSession(e.player)
        e.player.releaseDataContainer()
    }
}