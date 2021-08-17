package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.chat.ChatFormats
import me.arasple.mc.trchat.module.filter.ChatFilter
import me.arasple.mc.trchat.module.func.ChatFunctions
import me.arasple.mc.trchat.util.Updater
import me.arasple.mc.trchat.module.bungee.Bungees
import org.bukkit.Bukkit
import taboolib.common.env.Repository.downloadToFile
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.lang.sendLang
import taboolib.platform.BukkitPlugin
import java.io.File
import java.net.URL

/**
 * @author Arasple
 */
@PlatformSide([Platform.BUKKIT])
object TrChat : Plugin() {

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    fun getTrVersion(): Double = 1.81

    val motd = arrayOf(
        "",
        "§3  ___________      _________ .__            __",
        "§3  \\__    __________\\_   ___ \\|  |__ _____ _/  |_",
        "§3  |    |  \\_  __ /    \\  \\/|  |  \\\\__  \\\\   __\\",
        "§3  |    |   |  | \\\\     \\___|   Y  \\/ __ \\|  |",
        "§3  |____|   |__|   \\______  |___|  (____  |__|",
        "§3  \\/     \\/     \\/      "
    )

    override fun onLoad() {
        motd.forEach { l -> console().sendMessage(l) }
        console().sendLang("Plugin-Loaded")

        if (!hookPlaceholderAPI()) {
            return
        }
        // Updater
        Updater.init()
        // Chat Filter
        ChatFilter.loadFilter(true, console())
        // Chat Formats
        ChatFormats.loadFormats(console())
        // Chat Functions
        ChatFunctions.loadFunctions(console())
        // Bungees
        Bungees.init()
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)
    }

    override fun onDisable() {
        console().sendLang("Plugin-Disabled")
    }

    /**
     * 检测前置 PlaceholderAPI
     * 并自动下载、重启服务器
     */
    private fun hookPlaceholderAPI(): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")
        val jarFile = File("plugins/PlaceholderAPI.jar")
        val url = URL("https://api.spiget.org/v2/resources/6245/download")

        if (plugin == null) {
            jarFile.delete()
            console().sendLang("Plugin-Depend-Download", "PlaceholderAPI")
            try {
                downloadToFile(url, jarFile)
            } catch (e: Throwable) {
                e.printStackTrace()
                console().sendLang("Plugin-Depend-Install-Failed", "PlaceholderAPI")
                Bukkit.shutdown()
                return false
            }
            console().sendLang("Plugin-Depend-Installed", "PlaceholderAPI")
            Bukkit.shutdown()
            return false
        }
        return true
    }
}