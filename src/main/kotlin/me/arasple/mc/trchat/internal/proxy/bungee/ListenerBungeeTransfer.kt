package me.arasple.mc.trchat.internal.proxy.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.chat.ComponentSerializer
import net.md_5.bungee.command.ConsoleCommandSender
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.server
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import taboolib.module.porticus.common.MessageReader
import java.io.IOException
import java.util.*

/**
 * ListenerBungeeTransfer
 * me.arasple.mc.trchat.internal.proxy.bungee
 *
 * @author Arasple, wlys
 * @since 2021/8/9 15:01
 */
@PlatformSide([Platform.BUNGEE])
object ListenerBungeeTransfer {

    @SubscribeEvent
    fun onTransfer(e: PluginMessageEvent) {
        if (e.isCancelled) {
            return
        }
        if (e.tag == "trchat:main") {
            try {
                val message = MessageReader.read(e.data)
                if (message.isCompleted) {
                    val data = message.build()
                    execute(data)
                }
            } catch (_: IOException) {
            }
        }
    }

    private fun execute(data: Array<String>) {
        when (data[0]) {
            "SendRaw" -> {
                val to = data[1]
                val player = getProxyPlayer(to)

                if (player != null && player.cast<ProxiedPlayer>().isConnected) {
                    val raw = data[2]
                    player.sendRawMessage(raw)
                }
            }
            "BroadcastRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val message = ComponentSerializer.parse(raw)
                server<ProxyServer>().servers.forEach { (_, v) ->
                    v.players.forEach {
                        it.sendMessage(UUID.fromString(uuid), *message)
                    }
                }
                console().cast<ConsoleCommandSender>().sendMessage(*message)
            }
            "ForwardRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val ports = data[3].split(";").map { it.toInt() }
                val message = ComponentSerializer.parse(raw)
                server<ProxyServer>().servers.forEach { (_, v) ->
                    if (ports.contains(v.address.port)) {
                        v.players.forEach {
                            it.sendMessage(UUID.fromString(uuid), *message)
                        }
                    }
                }
                console().cast<ConsoleCommandSender>().sendMessage(*message)
            }
            "SendRawPerm" -> {
                val raw = data[1]
                val perm = data[2]

                onlinePlayers().filter { p -> p.hasPermission(perm) }.forEach { p ->
                    p.sendRawMessage(raw)
                }
            }
            "SendLang" -> {
                val to = data[1]
                val node = data[2]
                val args = subList(data.toList(), 3).toTypedArray()

                try {
                    getProxyPlayer(to)?.sendLang(node, *args)
                } catch (_: IllegalStateException) {
                }
            }
        }
    }
}