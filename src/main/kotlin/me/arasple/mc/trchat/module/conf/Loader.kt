package me.arasple.mc.trchat.module.conf

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * @author wlys
 * @since 2021/12/12 13:45
 */
object Loader {

    fun load(file: File) {
        val yaml = YamlConfiguration.loadConfiguration(file)
    }
}