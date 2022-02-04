package me.arasple.mc.trchat

import me.arasple.mc.trchat.internal.data.Database
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.lang.sendLang
import taboolib.platform.BukkitPlugin

/**
 * @author Arasple
 */
@PlatformSide([Platform.BUKKIT])
object TrChat : Plugin() {

    val plugin by lazy { BukkitPlugin.getInstance() }

    var isGlobalMuting = false

    override fun onLoad() {
        console().sendLang("Plugin-Loaded")
    }

    override fun onEnable() {
        Database.init()
        console().sendLang("Plugin-Enabled", pluginVersion)
        HookPlugin.printInfo()
    }
}