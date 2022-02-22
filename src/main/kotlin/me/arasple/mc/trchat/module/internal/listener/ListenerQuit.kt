package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.module.internal.data.Database
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit

/**
 * @author wlys
 * @since 2021/12/11 23:19
 */
@PlatformSide([Platform.BUKKIT])
object ListenerQuit {

    @SubscribeEvent(EventPriority.HIGHEST)
    fun e(e: PlayerQuitEvent) {
        val player = e.player

        Channel.channels.forEach { it.listeners.remove(player.uniqueId) }

        Settings.chatDelay.get().reset(player.name)
        Functions.itemShowDelay.get().reset(player.name)
        Functions.mentionDelay.get().reset(player.name)
        Functions.inventoryShowDelay.get().reset(player.name)

        ChatSession.removeSession(player)

        submit(async = true) {
            Database.database.push(player)
        }
    }
}