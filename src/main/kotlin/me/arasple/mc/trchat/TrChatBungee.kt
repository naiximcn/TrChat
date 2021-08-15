package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.updater.Updater
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.lang.sendLang
import taboolib.module.metrics.Metrics
import taboolib.platform.BungeePlugin

/**
 * @author Arasple
 * @date 2019/8/4 22:42
 */
@PlatformSide([Platform.BUNGEE])
object TrChatBungee : Plugin() {

    override fun onLoad() {
        TrChat.motd.forEach { l -> console().sendMessage(l) }
        console().sendLang("Plugin-Loaded")
        console().sendLang("Plugin-Registered-Bungee")
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)
        Metrics(5803, pluginVersion, Platform.BUNGEE)
    }

    override fun onDisable() {
        console().sendLang("Plugin-Disabled")
    }
}
