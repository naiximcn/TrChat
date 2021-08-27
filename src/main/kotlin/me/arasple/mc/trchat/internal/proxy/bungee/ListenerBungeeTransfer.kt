package me.arasple.mc.trchat.internal.proxy.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.server
import taboolib.module.lang.sendLang
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException

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
        try {
            val byteArray = ByteArrayInputStream(e.data)
            val data = DataInputStream(byteArray)

            val subChannel = data.readUTF()
            val type = data.readUTF()

            if (subChannel == "TrChat") {
                if (type == "SendRaw") {
                    val to = data.readUTF()
                    val player = getProxyPlayer(to)

                    if (player != null && player.cast<ProxiedPlayer>().isConnected) {
                        val raw = data.readUTF()
                        player.sendRawMessage(raw)
                    }
                }
                if (type == "BroadcastRaw") {
                    val raw = data.readUTF()
                    server<ProxyServer>().broadcast(*ComponentSerializer.parse(raw))
                }
                if (type == "SendRawPerm") {
                    val raw = data.readUTF()
                    val perm = data.readUTF()

                    onlinePlayers().filter { p -> p.hasPermission(perm) }.forEach { p ->
                        p.sendRawMessage(raw)
                    }
                }
                if (type == "SendLang") {
                    val to = data.readUTF()
                    val node = data.readUTF()
                    val arg = data.readUTF()

                    getProxyPlayer(to)?.sendLang(node, arg)
                }
            }
        } catch (ignored: IOException) {
        }
    }
}