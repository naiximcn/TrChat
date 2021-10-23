package me.arasple.mc.trchat.internal.listener

import me.arasple.mc.trchat.api.TrChatFiles
import org.bukkit.event.player.PlayerCommandSendEvent
import org.bukkit.event.server.TabCompleteEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent

/**
 * ListenerTabComplete
 * me.arasple.mc.trchat.internal.listener
 *
 * @author wlys
 * @since 2021/10/22 23:25
 */
@PlatformSide([Platform.BUKKIT])
object ListenerTabComplete {

    @SubscribeEvent
    fun e(e: PlayerCommandSendEvent) {
        if (TrChatFiles.settings.getBoolean("GENERAL.PREVENT-TAB-COMPLETE", false)
            && !e.player.hasPermission("trchat.bypass.tabcomplete")) {
            e.commands.clear()
        }
    }
}