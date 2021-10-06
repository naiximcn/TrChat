package me.arasple.mc.trchat

import com.google.common.io.ByteStreams
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import taboolib.module.metrics.Metrics
import taboolib.platform.VelocityPlugin
import java.io.IOException

/**
 * TrChatVelocity
 * me.arasple.mc.trchat
 *
 * @author wlys
 * @since 2021/8/21 13:42
 */
@PlatformSide([Platform.VELOCITY])
object TrChatVelocity : Plugin() {

    val plugin by lazy { VelocityPlugin.getInstance() }

    val motd = arrayOf(
        "",
        "§3  _______     §b _____  _              _   ",
        "§3 |__   __|    §b/ ____|| |           | |  ",
        "§3    | |  _ __ §b| |     | |__    __ _ | |_ ",
        "§3    `| | | '__|§b| |     | '_ \\  / _` || __|",
        "§3   | | |   | §b|____ | | | || (_| || |_ ",
        "§3    |_| |_|    §b\\_____||_| |_| \\__,_| \\__|",
    )

    lateinit var incoming: MinecraftChannelIdentifier
    lateinit var outgoing: MinecraftChannelIdentifier

    override fun onLoad() {
        motd.forEach { l -> console().sendMessage(l) }
        console().sendLang("Plugin-Loaded")
        console().sendLang("Plugin-Proxy-Velocity")

        incoming = MinecraftChannelIdentifier.create("trchat", "proxy").also {
            plugin.server.channelRegistrar.register(it)
        }
        outgoing = MinecraftChannelIdentifier.create("trchat", "server").also {
            plugin.server.channelRegistrar.register(it)
        }
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)
        Metrics(12541, pluginVersion, Platform.VELOCITY)

        submit(period = 60, async = true) {
            val out = ByteStreams.newDataOutput()
            try {
                out.writeUTF("PlayerList")
                out.writeUTF(onlinePlayers().joinToString(", ") { it.name })
            } catch (e: IOException) {
                e.printStackTrace()
            }
            plugin.server.allServers.forEach { server ->
                server.sendPluginMessage(outgoing, out.toByteArray())
            }
        }
    }

    override fun onDisable() {
        console().sendLang("Plugin-Disabled")
    }
}