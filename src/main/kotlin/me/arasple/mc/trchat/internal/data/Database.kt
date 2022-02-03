package me.arasple.mc.trchat.internal.data

import me.arasple.mc.trchat.api.TrChatFiles.settings
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.io.newFile
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.releaseDataContainer
import taboolib.expansion.setupDataContainer
import taboolib.expansion.setupPlayerDatabase
import java.io.File

/**
 * Database
 * me.arasple.mc.trchat.internal.data
 *
 * @author wlys
 * @since 2021/9/11 13:29
 */
@PlatformSide([Platform.BUKKIT])
object Database {

    fun init() {
        if (settings.getBoolean("database.enable")) {
            setupPlayerDatabase(settings.getConfigurationSection("database")!!)
        } else {
            setupPlayerDatabase(newFile(File(getDataFolder(), "data"), "data.db"))
        }
    }

    @SubscribeEvent(EventPriority.LOWEST)
    fun e(e: PlayerJoinEvent) {
        e.player.setupDataContainer()
    }

    @SubscribeEvent(EventPriority.HIGHEST)
    fun e(e: PlayerQuitEvent) {
        e.player.releaseDataContainer()
    }
}