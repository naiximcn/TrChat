package me.arasple.mc.trchat.api

import me.arasple.mc.trchat.common.channel.ChatChannels
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.filter.ChatFilter
import me.arasple.mc.trchat.common.function.ChatFunctions
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile

/**
 * @author Arasple, wlys
 * @date 2019/11/30 9:59
 */
@PlatformSide([Platform.BUKKIT])
object TrChatFiles {

    @Config("settings.yml", autoReload = true)
    lateinit var settings: ConfigFile
        private set

    @Config("formats.yml", autoReload = true)
    lateinit var formats: ConfigFile
        private set

    @Config("filter.yml", autoReload = true)
    lateinit var filter: ConfigFile
        private set

    @Config("function.yml", autoReload = true)
    lateinit var function: ConfigFile
        private set

    @Config("channels.yml", autoReload = true)
    lateinit var channels: ConfigFile
        private set

    @Awake(LifeCycle.LOAD)
    fun migrate() {
        val migrations = mapOf(
            settings to Pair("GENERAL.DATABASE", mapOf(
                "enable" to false,
                "host" to "localhost",
                "port" to 3306,
                "user" to "root",
                "password" to "root",
                "database" to "root",
                "table" to "trchat"
            )),
            filter to Pair("FILTER", mapOf(
                "CHAT" to true,
                "SIGN" to true,
                "ANVIL" to true,
                "ITEM" to false
            ))
        )
        migrations.entries.forEach { (config, value) ->
            if (!config.contains(value.first)) {
                config[value.first] = value.second
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun init() {
        formats.onReload { ChatFormats.loadFormats(console()) }
        filter.onReload { ChatFilter.loadFilter(false, console()) }
        function.onReload { ChatFunctions.loadFunctions(console()) }
        channels.onReload { ChatChannels.loadChannels(console()) }
    }

    fun reloadAll() {
        settings.reload()
        formats.reload()
        filter.reload()
        function.reload()
        channels.reload()
    }
}