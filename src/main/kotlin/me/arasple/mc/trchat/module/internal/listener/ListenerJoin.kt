package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.module.display.Channel
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent

/**
 * @author wlys
 * @since 2021/12/11 23:19
 */
object ListenerJoin {

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        Channel.channels.filter { it.settings.autoJoin }.forEach {
            it.listeners.add(e.player.name)
        }
    }
}