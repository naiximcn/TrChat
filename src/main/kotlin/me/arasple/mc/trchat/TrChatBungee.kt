package me.arasple.mc.trchat

import com.google.common.io.ByteStreams
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.ProxyServer
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.common.platform.function.server
import taboolib.module.lang.sendLang
import taboolib.module.metrics.Metrics
import taboolib.platform.BungeePlugin
import java.io.IOException

/**
 * @author Arasple
 * @date 2019/8/4 22:42
 */
@PlatformSide([Platform.BUNGEE])
@RuntimeDependencies(
    RuntimeDependency("!net.kyori:adventure-api:4.9.3", test = "net.kyori.adventure.Adventure"),
    RuntimeDependency("!net.kyori:adventure-platform-bungeecord:4.0.1")
)
object TrChatBungee : Plugin() {

    val plugin by lazy { BungeePlugin.getInstance() }

    lateinit var adventure: BungeeAudiences
        private set

    const val TRCHAT_CHANNEL = "trchat:main"

    override fun onLoad() {
        console().sendLang("Plugin-Loaded")
        console().sendLang("Plugin-Proxy-Supported", "Bungee")
        ProxyServer.getInstance().registerChannel(TRCHAT_CHANNEL)
        Metrics(5803, pluginVersion, Platform.BUNGEE)
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)
        adventure = BungeeAudiences.create(plugin)

        command("muteallservers", permission = "trchat.mute") {
            dynamic("state") {
                suggestion<ProxyCommandSender> { _, _ ->
                    listOf("on", "off")
                }
                execute<ProxyCommandSender> { _, _, argument ->
                    val out = ByteStreams.newDataOutput()
                    try {
                        out.writeUTF("GlobalMute")
                        out.writeUTF(argument)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    server<ProxyServer>().servers.forEach { (_, v) ->
                        v.sendData(TRCHAT_CHANNEL, out.toByteArray())
                    }
                }
            }
        }
    }

    override fun onDisable() {
        console().sendLang("Plugin-Disabled")
    }
}
