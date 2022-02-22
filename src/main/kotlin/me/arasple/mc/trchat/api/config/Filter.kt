package me.arasple.mc.trchat.api.config

import me.arasple.mc.trchat.module.display.filter.ChatFilter
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

/**
 * @author wlys
 * @since 2022/2/4 13:04
 */
@PlatformSide([Platform.BUKKIT])
object Filter {

    @Config("filter.yml", autoReload = true)
    lateinit var CONF: Configuration
        private set

    @Awake(LifeCycle.LOAD)
    fun init() {
        CONF.onReload {
            ChatFilter.loadFilter(true)
        }
    }
}