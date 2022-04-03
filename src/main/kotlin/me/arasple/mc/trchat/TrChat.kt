package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.display.filter.ChatFilter
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.util.Util
import me.arasple.mc.trchat.util.proxy.Proxy
import org.bukkit.Bukkit
import taboolib.common.env.RuntimeEnv
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.platform.BukkitPlugin

/**
 * @author Arasple
 */
@PlatformSide([Platform.BUKKIT])
object TrChat : Plugin() {

    val plugin by lazy { BukkitPlugin.getInstance() }

    var paperEnv = false
        private set

    var isGlobalMuting = false

    val reportedErrors = mutableListOf<String>()

    @Awake
    fun loadDependency() {
        try {
            // Paper 1.16.5+
            Class.forName("com.destroystokyo.paper.PaperConfig")
            if (majorLegacy >= 11604) {
                paperEnv = true
            }
        } catch (_: ClassNotFoundException) {
        }
        if (!paperEnv) {
            RuntimeEnv.ENV.loadDependency(BukkitEnv::class.java, true)
        }
    }

    override fun onLoad() {
        console().sendLang("Plugin-Loading", Bukkit.getBukkitVersion())
    }

    override fun onEnable() {
        if (!paperEnv) {
            Util.init()
        }

        Loader.loadChannels(console())
        Loader.loadFunctions(console())
        ChatFilter.loadFilter(true, console())

        Proxy.init()
        Database.init()
        HookPlugin.printInfo()
        console().sendLang("Plugin-Enabled", pluginVersion)
    }

    override fun onDisable() {
        if (!paperEnv) {
            Util.release()
        }
    }
}