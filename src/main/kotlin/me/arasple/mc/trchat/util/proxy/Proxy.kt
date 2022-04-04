package me.arasple.mc.trchat.util.proxy

import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.proxy.bungee.Bungees
import me.arasple.mc.trchat.util.proxy.common.MessageBuilder
import me.arasple.mc.trchat.util.proxy.velocity.Velocity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang
import java.util.*

/**
 * Proxy
 * me.arasple.mc.trchat.util.proxy
 *
 * @author wlys
 * @since 2021/8/21 13:24
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Proxy {

    var isEnabled = false

    val platform by lazy {
        if (Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord")) {
            isEnabled = true
            console().sendLang("Plugin-Proxy-Supported", "Bungee")
            Platform.BUNGEE
        } else if (kotlin.runCatching {
                Bukkit.getServer().spigot().paperConfig.getBoolean("settings.velocity-support.enabled")
        }.getOrDefault(false)) {
            isEnabled = true
            console().sendLang("Plugin-Proxy-Supported", "Velocity")
            Platform.VELOCITY
        } else {
            console().sendLang("Plugin-Proxy-None")
            Platform.UNKNOWN
        }
    }

    fun init() {
        when (platform) {
            Platform.BUNGEE -> Bungees.init()
            Platform.VELOCITY -> Velocity.init()
            else -> return
        }
    }

    fun sendBukkitMessage(player: Player, vararg args: String) {
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
            when (platform) {
                Platform.BUNGEE -> sendBukkitMessage(player, "SendLang", target, node, *args)
                Platform.VELOCITY -> sendBukkitMessage(player, "SendLang", target, node, *args)
                else -> return
            }
        }
    }
}

fun Player.sendBukkitMessage(vararg args: String) {
    Proxy.sendBukkitMessage(this, *args)
}

fun Player.sendProxyLang(target: String, node: String, vararg args: String) {
    Proxy.sendProxyLang(this, target, node, *args)
}

fun Array<out String>.serialize() = MessageBuilder.create(arrayOf(UUID.randomUUID().toString(), *this))