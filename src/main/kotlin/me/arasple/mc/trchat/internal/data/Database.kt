package me.arasple.mc.trchat.internal.data

import me.arasple.mc.trchat.api.TrChatFiles.settings
import taboolib.common.io.newFile
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.getDataFolder
import taboolib.expansion.setupPlayerDatabase
import java.io.File

/**
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
}