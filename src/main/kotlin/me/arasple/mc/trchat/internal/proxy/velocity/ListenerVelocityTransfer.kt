package me.arasple.mc.trchat.internal.proxy.velocity

import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import me.arasple.mc.trchat.TrChatVelocity
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.server
import java.io.IOException

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
        if (e.identifier != TrChatVelocity.incoming) {
            return
        }
        try {
            val data = e.dataAsDataStream()

            val type = data.readUTF()

            if (type == "SendRaw") {
                val to = data.readUTF()
                val player = getProxyPlayer(to)

                if (player != null && player.cast<Player>().currentServer.isPresent) {
                    val raw = data.readUTF()
                    player.sendRawMessage(raw)
                }
            }
            if (type == "BroadcastRaw") {
                val raw = data.readUTF()
                val message = GsonComponentSerializer.gson().deserialize(raw)
                server<ProxyServer>().sendMessage(message)
                console().cast<ConsoleCommandSource>().sendMessage(message)
            }
            if (type == "SendRawPerm") {
                val raw = data.readUTF()
                val perm = data.readUTF()

                onlinePlayers().filter { p -> p.hasPermission(perm) }.forEach { p ->
                    p.sendRawMessage(raw)
                }
            }
        } catch (ignored: IOException) {
        }
    }
}