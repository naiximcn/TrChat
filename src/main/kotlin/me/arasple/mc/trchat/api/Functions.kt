package me.arasple.mc.trchat.api

import taboolib.common5.Baffle
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.configuration.Configuration
import java.util.concurrent.TimeUnit

/**
 * @author wlys
 * @since 2021/12/12 11:40
 */
object Functions {

    @Config("function.yml", autoReload = true)
    lateinit var CONF: Configuration
        private set

    @ConfigNode("General.Item-Show.Cooldowns", "function.yml")
    val itemShowDelay = ConfigNodeTransfer<Double, Baffle> { Baffle.of((this * 1000).toLong(), TimeUnit.MILLISECONDS) }

    @ConfigNode("General.Mention.Cooldowns", "function.yml")
    val mentionDelay = ConfigNodeTransfer<Double, Baffle> { Baffle.of((this * 1000).toLong(), TimeUnit.MILLISECONDS) }

    val itemShow by lazy {
        CONF.getConfigurationSection("General.Item-Show")!!
    }

    val mention by lazy {
        CONF.getConfigurationSection("General.Mention")!!
    }
}