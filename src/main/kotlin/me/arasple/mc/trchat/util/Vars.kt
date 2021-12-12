package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.api.Settings
import me.clip.placeholderapi.PlaceholderAPIPlugin
import org.bukkit.Bukkit
import taboolib.common.env.Repository
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * @author Arasple
 * @date 2019/11/29 21:29
 */
@PlatformSide([Platform.BUKKIT])
object Vars {

    /**
     * 检测前置 PlaceholderAPI
     * 并自动下载、重启服务器
     */
    internal fun hookPlaceholderAPI(): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")
        val jarFile = File("plugins/PlaceholderAPI.jar")
        val url = URL("https://api.spiget.org/v2/resources/6245/download")

        if (plugin == null) {
            jarFile.delete()
            console().sendLang("Plugin-Depend-Download", "PlaceholderAPI")
            try {
                Repository.downloadToFile(url, jarFile)
            } catch (e: IOException) {
                e.printStackTrace()
                console().sendLang("Plugin-Depend-Install-Failed", "PlaceholderAPI")
                return false
            }
            console().sendLang("Plugin-Depend-Installed", "PlaceholderAPI")
            return false
        }
        return true
    }

    @Schedule(async = true)
    fun downloadExpansions() {
        downloadExpansions(Settings.CONF.getStringList("Options.Depend-Expansions"))
    }

    /**
     * 自动下载 PlaceholderAPI 拓展变量并注册
     *
     * @param expansions 拓展
     */
    private fun downloadExpansions(expansions: List<String>) {
        kotlin.runCatching {
            if (expansions.isNotEmpty()) {
                if (PlaceholderAPIPlugin.getInstance().cloudExpansionManager.cloudExpansions.isEmpty()) {
                    PlaceholderAPIPlugin.getInstance().cloudExpansionManager.fetch(false)
                }
                val unInstalled = expansions.filter { d ->
                    PlaceholderAPIPlugin.getInstance().localExpansionManager.expansions.none { e -> e.name.equals(d, ignoreCase = true) }
                            && PlaceholderAPIPlugin.getInstance().cloudExpansionManager.findCloudExpansionByName(d).isPresent
                            && !PlaceholderAPIPlugin.getInstance().cloudExpansionManager.isDownloading(
                        PlaceholderAPIPlugin.getInstance().cloudExpansionManager.findCloudExpansionByName(d).get()
                    )
                }
                if (unInstalled.isNotEmpty()) {
                    unInstalled.forEach { ex ->
                        val cloudExpansion = PlaceholderAPIPlugin.getInstance().cloudExpansionManager.cloudExpansions[ex]!!
                        PlaceholderAPIPlugin.getInstance().cloudExpansionManager.downloadExpansion(cloudExpansion, cloudExpansion.version)
                    }
                    submit(delay = 20) {
                        PlaceholderAPIPlugin.getInstance().localExpansionManager.expansions.forEach { PlaceholderAPIPlugin.getInstance().localExpansionManager.register(it) }
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }
}