package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
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
@RuntimeDependencies(
    RuntimeDependency("!net.kyori:adventure-api:4.9.3", test = "net.kyori.adventure.Adventure"),
    RuntimeDependency("!net.kyori:adventure-platform-bukkit:4.0.1")
)
object TrChat : Plugin() {

    val plugin by lazy { BukkitPlugin.getInstance() }

    lateinit var adventure: BukkitAudiences
        private set

    var isGlobalMuting = false

    override fun onLoad() {
        console().sendLang("Plugin-Loaded")
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)
        adventure = BukkitAudiences.create(plugin)
        Database.init()
        HookPlugin.printInfo()
    }

    override fun onDisable() {
        adventure.close()
    }
}