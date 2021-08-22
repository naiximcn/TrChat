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

    val plugin by lazy {
        VelocityPlugin.getInstance()
    }

    lateinit var incoming: MinecraftChannelIdentifier
    lateinit var outgoing: MinecraftChannelIdentifier

    override fun onLoad() {
        TrChat.motd.forEach { l -> console().sendMessage(l) }
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

        submit(period = 60, async = true) {
            plugin.server.allServers.forEach { server ->
                val out = ByteStreams.newDataOutput()
                try {
                    out.writeUTF("PlayerList")
                    out.writeUTF(onlinePlayers().joinToString(", ") { it.name })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                server.sendPluginMessage(outgoing, out.toByteArray())
            }
        }
    }

    override fun onDisable() {
        console().sendLang("Plugin-Disabled")
    }
}