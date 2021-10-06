package me.arasple.mc.trchat.internal.proxy

import me.arasple.mc.trchat.api.TrChatFiles
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang

/**
 * Proxy
 * me.arasple.mc.trchat.internal.proxy
 *
 * @author wlys
 * @since 2021/8/21 13:24
 */
@PlatformSide([Platform.BUKKIT])
object Proxy {

    var isEnabled = false

    val platform by lazy {
        if (Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord", false)) {
            TrChatFiles.settings.set("GENERAL.PROXY", "BUNGEE")
        }
        when (val p = TrChatFiles.settings.getString("GENERAL.PROXY", "NONE").uppercase()) {
            "NONE" -> {
                console().sendLang("Plugin-Proxy-None")
                Platform.UNKNOWN
            }
            "BUNGEE" -> {
                console().sendLang("Plugin-Proxy-Supported", "Bungee")
                Platform.BUNGEE
            }
            "VELOCITY" -> {
                console().sendLang("Plugin-Proxy-Supported", "Velocity")
                Platform.VELOCITY
            }
            else -> error("Unsupported proxy $p.")
        }
    }

    fun init() {
        when (platform) {
            Platform.BUNGEE -> Bungees.init()
            Platform.VELOCITY -> Velocity.init()
            else -> return
        }
    }

    fun sendProxyMessage(player: Player, vararg args: String) {
        when (platform) {
            Platform.BUNGEE -> Bungees.sendBukkitMessage(player, *args)
            Platform.VELOCITY -> Velocity.sendBukkitMessage(player, *args)
            else -> return
        }
    }

    fun sendProxyLang(player: Player, target: String, node: String, vararg args: String) {
        if (!isEnabled || Bukkit.getPlayerExact(target) != null) {
            getProxyPlayer(target)?.sendLang(node, *args)
        } else {
            try {
                when (platform) {
                    Platform.BUNGEE -> sendProxyMessage(player, "SendLang", target, node, *args)
                    Platform.VELOCITY -> sendProxyMessage(player, "SendLang", target, node, *args)
                    else -> return
                }
            } catch (ignored: IllegalStateException) {
            }
        }
    }
}

fun Player.sendBukkitMessage(vararg args: String) {
    Proxy.sendProxyMessage(this, *args)
}