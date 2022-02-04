package me.arasple.mc.trchat.api.config

import taboolib.common5.Baffle
import taboolib.library.configuration.ConfigurationSection
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

    @ConfigNode("General.Item-Show.Keys", "function.yml")
    val itemShowKeys = ConfigNodeTransfer<List<String>, List<Regex>> { map { Regex("$it(-[1-9])?") } }

    @ConfigNode("General.Mention.Cooldowns", "function.yml")
    val mentionDelay = ConfigNodeTransfer<Double, Baffle> { Baffle.of((this * 1000).toLong(), TimeUnit.MILLISECONDS) }

    @ConfigNode("General.Item-Show", "function.yml")
    lateinit var itemShow: ConfigurationSection

    @ConfigNode("General.Mention", "function.yml")
    lateinit var mention: ConfigurationSection

    @ConfigNode("General.Inventory-Show", "function.yml")
    lateinit var inventoryShow: ConfigurationSection
}