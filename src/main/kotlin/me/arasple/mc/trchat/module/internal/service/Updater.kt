package me.arasple.mc.trchat.module.internal.service

import com.google.gson.JsonParser
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.LifeCycle
import taboolib.common.env.DependencyDownloader.readFully
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.SkipTo
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.submit
import taboolib.common.util.Version
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author Arasple
 * @date 2019/11/29 21:04
 */
@PlatformSide([Platform.BUKKIT])
@SkipTo(LifeCycle.LOAD)
object Updater {

    private const val api_url = "https://api.github.com/repos/FlickerProjects/TrChat/releases/latest"
    private val notified = mutableListOf<UUID>()
    private var notify = false
    private val current_version = Version(pluginVersion)
    private var latest_Version = Version("0.0")
    private var information = ""

//    @Awake(LifeCycle.LOAD)
    fun init() {
        submit(delay = 20, period = (15 * 60 * 20).toLong(), async = true) {
            grabInfo()
        }
    }

    private fun notifyVersion() {
        if (latest_Version > current_version) {
            console().sendLang("Plugin-Updater-Header", current_version, latest_Version)
            console().sendMessage(information)
            console().sendLang("Plugin-Updater-Footer")
        } else {
            if (!notify) {
                notify = true
                if (current_version > latest_Version) {
                    console().sendLang("Plugin-Updater-Dev")
                } else {
                    console().sendLang("Plugin-Updater-Latest")
                }
            }
        }
    }

    private fun grabInfo() {
        if (latest_Version.version[0] > 0) {
            return
        }
        kotlin.runCatching {
            URL(api_url).openStream().use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val read = readFully(bufferedInputStream, StandardCharsets.UTF_8)
                    val json = JsonParser.parseString(read).asJsonObject
                    val latestVersion = json["tag_name"].asString
                    latest_Version = Version(latestVersion)
                    information = json["body"].asString
                    notifyVersion()
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        if (player.hasPermission("trchat.admin") && latest_Version > current_version && !notified.contains(player.uniqueId)) {
            player.sendLang("Plugin-Updater-Header", current_version, latest_Version)
            player.sendMessage(information)
            player.sendLang("Plugin-Updater-Footer")
            notified.add(player.uniqueId)
        }
    }
}