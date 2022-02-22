package me.arasple.mc.trchat.api.config

import me.arasple.mc.trchat.module.display.channel.Channel
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common5.Baffle
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.configuration.Configuration
import java.util.concurrent.TimeUnit

/**
 * @author wlys
 * @since 2021/12/11 23:59
 */
@PlatformSide([Platform.BUKKIT])
object Settings {

    @Config("settings.yml", autoReload = true)
    lateinit var CONF: Configuration
        private set

    @ConfigNode("Chat.Cooldown", "settings.yml")
    val chatDelay = ConfigNodeTransfer<Double, Baffle> { Baffle.of((this * 1000).toLong(), TimeUnit.MILLISECONDS) }

    @ConfigNode("Chat.Anti-Repeat", "settings.yml")
    var chatSimilarity = 0.85

    @ConfigNode("Chat.Length-Limit", "settings.yml")
    var chatLengthLimit = 100

    @ConfigNode("Channel.Default", "settings.yml")
    val channelDefault = ConfigNodeTransfer<String, Channel?> { Channel.channels.firstOrNull { it.id == this } }
}