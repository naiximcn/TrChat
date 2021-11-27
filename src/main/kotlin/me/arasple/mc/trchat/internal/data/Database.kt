package me.arasple.mc.trchat.internal.data

import me.arasple.mc.trchat.api.TrChatFiles.settings
import me.arasple.mc.trchat.internal.database.DatabaseLocal
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.io.newFile
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.getDataContainer
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

    var old_database: DatabaseLocal? = null

    fun init() {
        if (!settings.contains("GENERAL.DATABASE")) {
            settings["GENERAL.DATABASE"] = mapOf(
                "enable" to false,
                "host" to "localhost",
                "port" to 3306,
                "user" to "root",
                "password" to "root",
                "database" to "root",
                "table" to "trchat"
            )
        }
        if (settings.getBoolean("database.enable")) {
            setupPlayerDatabase(settings.getConfigurationSection("database")!!)
        } else {
            setupPlayerDatabase(newFile(File(getDataFolder(), "data"), "data.db"))
        }
        if (File(getDataFolder(), "data.db").exists()) {
            old_database = DatabaseLocal()
        }
    }

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        e.player.setupDataContainer()
        // Migrate
        old_database?.let { database ->
            database.pull(e.player)?.run {
                e.player.getDataContainer()["filter"] = getBoolean("FILTER", false)
                e.player.getDataContainer()["mute_time"] = getLong("MUTE_TIME", 0)
                e.player.getDataContainer()["custom_channel"] = getString("CUSTOM-CHANNEL", "")!!
            }
            database.release(e.player)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        e.player.releaseDataContainer()
    }
}