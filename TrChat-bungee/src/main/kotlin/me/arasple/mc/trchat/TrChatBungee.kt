package me.arasple.mc.trchat

import com.google.common.io.ByteStreams
import net.md_5.bungee.api.ProxyServer
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
import java.io.IOException

/**
 * @author Arasple
 * @date 2019/8/4 22:42
 */
@PlatformSide([Platform.BUNGEE])
object TrChatBungee : Plugin() {

    val motd = arrayOf(
        "",
        "§3  _______     §b _____  _              _   ",
        "§3 |__   __|    §b/ ____|| |           | |  ",
        "§3    | |  _ __ §b| |     | |__    __ _ | |_ ",
        "§3    `| | | '__|§b| |     | '_ \\  / _` || __|",
        "§3   | | |   | §b|____ | | | || (_| || |_ ",
        "§3    |_| |_|    §b\\_____||_| |_| \\__,_| \\__|",
    )

    override fun onLoad() {
        motd.forEach { l -> console().sendMessage(l) }
        ProxyServer.getInstance().registerChannel("trchat:main")
        console().sendLang("Plugin-Loaded")
        console().sendLang("Plugin-Proxy-Bungee")
    }

    override fun onEnable() {
        console().sendLang("Plugin-Enabled", pluginVersion)
        Metrics(5803, pluginVersion, Platform.BUNGEE)

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
                        v.sendData("BungeeCord", out.toByteArray())
                    }
                }
            }
        }
    }

    override fun onDisable() {
        console().sendLang("Plugin-Disabled")
    }
}
