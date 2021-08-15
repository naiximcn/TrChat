package me.arasple.mc.trchat.module.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException

/**
 * ListenerBungeeTransfer
 * me.arasple.mc.trchat.module.bungee
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
                    val player = ProxyServer.getInstance().players.firstOrNull { p ->
                        p.name.equals(to, ignoreCase = true)
                    }

                    if (player != null && player.isConnected) {
                        val raw = data.readUTF()
                        player.sendMessage(*ComponentSerializer.parse(raw))
                    }
                }
                if (type == "BroadcastRaw") {
                    val raw = data.readUTF()
                    ProxyServer.getInstance().broadcast(*ComponentSerializer.parse(raw))
                }
                if (type == "SendRawPerm") {
                    val raw = data.readUTF()
                    val perm = data.readUTF()

                    ProxyServer.getInstance().players.filter { p -> p.hasPermission(perm) }.forEach { p ->
                        p.sendMessage(*ComponentSerializer.parse(raw))
                    }
                }
            }
        } catch (ignored: IOException) {
        }
    }
}