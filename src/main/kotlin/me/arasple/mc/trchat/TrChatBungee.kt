package me.arasple.mc.trchat

import com.google.common.io.ByteStreams
import net.md_5.bungee.api.ProxyServer
import taboolib.common.env.RuntimeEnv
import taboolib.common.platform.*
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
object TrChatBungee : Plugin() {

    val plugin by lazy { BungeePlugin.getInstance() }

    const val TRCHAT_CHANNEL = "trchat:main"

    @Awake
    fun loadDependency() {
        RuntimeEnv.ENV.loadDependency(BungeeEnv::class.java, true)
    }

    override fun onLoad() {
        ProxyServer.getInstance().registerChannel(TRCHAT_CHANNEL)

        console().sendLang("Plugin-Loading", server<ProxyServer>().version)
        console().sendLang("Plugin-Proxy-Supported", "Bungee")

        Metrics(5803, pluginVersion, Platform.BUNGEE)
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)

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
