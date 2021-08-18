package me.arasple.mc.trchat.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.TrChatFiles
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.LifeCycle
import taboolib.common.env.DependencyDownloader.readFully
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.*
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
object Updater {

    private var isCheckUpdate = true
    private val noticed = mutableListOf<UUID>()
    private var url: String? = null
    private var version: Double? = null
    private var isOld = false
    private var newVersion: Double? = null
    private var isNoticedConsole = false
    private var information: String? = null

    fun init() {
        isCheckUpdate = TrChatFiles.settings.getBoolean("GENERAL.CHECK-UPDATE")
        url = "https://api.github.com/repos/FlickerProjects/$pluginId/releases/latest"
        version = TrChat.getTrVersion()
        newVersion = version

        if (!version.toString().equals(pluginVersion.split("-")[0], ignoreCase = true)) {
            console().sendLang("Error-Version")
            disablePlugin()
        }
    }

    private fun notifyVersion() {
        if (isOld) {
            console().sendLang("Plugin-Updater-Header", version!!, newVersion!!)
            console().sendMessage(information!!)
            console().sendLang("Plugin-Updater-Footer")
            if (newVersion!! - version!! >= 0.2) {
                console().sendLang("Plugin-Updater-Too-Old")
            }
        } else {
            if (!isNoticedConsole) {
                if (version!! > newVersion!!) {
                    console().sendLang("Plugin-Updater-Dev")
                } else {
                    console().sendLang("Plugin-Updater-Latest")
                }
                isNoticedConsole = true
            }
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun grabInfo() {
        if (!isCheckUpdate) {
            return
        }
        submit(delay = 20, period = 10 * 60 * 20, async = true) {
            if (isOld) {
                return@submit
            }
            try {
                URL(url).openStream().use { inputStream ->
                    BufferedInputStream(inputStream).use { bufferedInputStream ->
                        val read = readFully(bufferedInputStream, StandardCharsets.UTF_8)
                        val json = JsonParser().parse(read) as JsonObject
                        val latestVersion = json["tag_name"].asDouble
                        if (latestVersion > version!!) {
                            isOld = true
                        }
                        newVersion = latestVersion
                        information = json["body"].asString
                    }
                }
            } catch (ignored: Exception) {
            }
            submit { notifyVersion() }
        }
    }

    @SubscribeEvent
    fun onJoin(e: PlayerJoinEvent) {
        val p = e.player

        if (isOld && !noticed.contains(p.uniqueId) && p.hasPermission("trmenu.admin")) {
            noticed.add(p.uniqueId)
            submit(delay = 1) { p.sendLang("Plugin-Updater-Old", newVersion!!) }
        }
    }
}