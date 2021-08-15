package me.arasple.mc.trchat.module.chat.listeners

import org.bukkit.event.player.PlayerCommandSendEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent

/**
 * @author Arasple
 * @date 2020/1/17 14:41
 */
@PlatformSide([Platform.BUKKIT])
object ListenerTabComplete {

    @SubscribeEvent
    fun onTabCommandSend(e: PlayerCommandSendEvent) {
        val p = e.player
        if (!p.hasPermission("trchat.bypass.tabcomplete")) {
            e.commands.clear()
        }
    }
}