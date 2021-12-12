package me.arasple.mc.trchat.module.listener

import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.expansion.setupDataContainer

/**
 * @author wlys
 * @since 2021/12/11 23:19
 */
object ListenerJoin {

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        e.player.setupDataContainer()
    }
}