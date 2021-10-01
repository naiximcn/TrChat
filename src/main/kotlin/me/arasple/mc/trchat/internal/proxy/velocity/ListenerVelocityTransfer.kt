package me.arasple.mc.trchat.internal.proxy.velocity

import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.arasple.mc.trchat.TrChatVelocity
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
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
 * ListenerVelocityTransfer
 * me.arasple.mc.trchat.internal.proxy.velocity
 *
 * @author wlys
 * @since 2021/8/21 13:29
 */
@PlatformSide([Platform.VELOCITY])
object ListenerVelocityTransfer {

    @SubscribeEvent
    fun onTransfer(e: PluginMessageEvent) {
        if (e.identifier == TrChatVelocity.incoming) {
            try {
                val message = MessageReader.read(e.data)
                if (message.isCompleted) {
                    val data = message.build()
                    execute(data)
                }
            } catch (ignored: IOException) {
            }
        }
    }

    private fun execute(data: Array<String>) {
        when (data[0]) {
            "SendRaw" -> {
                val to = data[1]
                val player = getProxyPlayer(to)

                if (player != null && player.cast<Player>().currentServer.isPresent) {
                    val raw = data[2]
                    player.sendRawMessage(raw)
                }
            }
            "BroadcastRaw" -> {
                val uuid = data[1]
                val raw = data[2]
                val message = GsonComponentSerializer.gson().deserialize(raw)
                server<ProxyServer>().allServers.forEach { server ->
                    server.playersConnected.forEach { player ->
                        getProxyPlayer(UUID.fromString(uuid))?.cast<Player>()?.let {
                            player.sendMessage(it, message, MessageType.CHAT)
                        } ?: kotlin.run {
                            player.sendMessage(message, MessageType.CHAT)
                        }
                    }
                }
                console().cast<ConsoleCommandSource>().sendMessage(message)
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

                getProxyPlayer(to)?.sendLang(node, *args)
            }
        }
    }
}