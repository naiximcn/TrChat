package me.arasple.mc.trchat.api.config

import me.arasple.mc.trchat.module.display.filter.ChatFilter
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

/**
 * @author wlys
 * @since 2022/2/4 13:04
 */
object Filter {

    @Config("filter.yml", autoReload = true)
    lateinit var CONF: Configuration
        private set

    init {
        CONF.onReload {
            ChatFilter.loadFilter(true)
        }
    }
}