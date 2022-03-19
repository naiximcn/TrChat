package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.util.proxy.Proxy
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
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
@RuntimeDependencies(
    RuntimeDependency(
        value = "!net.kyori:adventure-api:4.10.0",
        test = "!net.kyori.adventure.Adventure"
    ),
    RuntimeDependency(
        value = "!net.kyori:adventure-platform-bukkit:4.1.0",
        test = "!net.kyori.adventure.platform.bukkit.BukkitAudiences",
        repository = "https://repo.maven.apache.org/maven2"
    ),
    RuntimeDependency(
        value = "!net.kyori:adventure-platform-bungeecord:4.1.0",
        test = "!net.kyori.adventure.platform.bungeecord.BungeeAudiences",
        repository = "https://repo.maven.apache.org/maven2"
    )
)
@PlatformSide([Platform.BUKKIT])
object TrChat : Plugin() {

    val plugin by lazy { BukkitPlugin.getInstance() }

    lateinit var adventure: BukkitAudiences
        private set

    var isGlobalMuting = false

    override fun onLoad() {
        console().sendLang("Plugin-Loading", Bukkit.getBukkitVersion())
    }

    override fun onEnable() {
        adventure = BukkitAudiences.create(plugin)

        Loader.loadChannels(console())
        Loader.loadFunctions(console())

        Proxy.init()
        Database.init()
        HookPlugin.printInfo()
        console().sendLang("Plugin-Enabled", pluginVersion)
    }

    override fun onDisable() {
        adventure.close()
    }
}