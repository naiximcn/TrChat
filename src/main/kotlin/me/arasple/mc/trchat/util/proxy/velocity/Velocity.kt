package me.arasple.mc.trchat.util.proxy.velocity

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.util.Internal
import me.arasple.mc.trchat.util.proxy.bukkit.Players
import me.arasple.mc.trchat.util.proxy.serialize
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import taboolib.common.platform.function.submit
import java.io.IOException

/**
 * Velocity
 * me.arasple.mc.trchat.util.proxy.velocity
 *
 * @author wlys
 * @since 2021/8/21 19:15
 */
@Internal
class Velocity : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != INCOMING_CHANNEL) {
            return
        }
        val data = ByteStreams.newDataInput(message)
        try {
            val subChannel = data.readUTF()
            if (subChannel == "PlayerList") {
                Players.setPlayers(data.readUTF().split(", "))
            }
        } catch (ignored: IOException) {
        }
    }

    companion object {

        const val INCOMING_CHANNEL = "trchat:server"
        const val OUTGOING_CHANNEL = "trchat:proxy"

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, OUTGOING_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, OUTGOING_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChat.plugin, INCOMING_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, INCOMING_CHANNEL, Velocity())
            }
        }

        fun sendBukkitMessage(player: Player, vararg args: String, async: Boolean = true) {
            submit(async = async) {
                try {
                    for (bytes in args.serialize()) {
                        player.sendPluginMessage(TrChat.plugin, OUTGOING_CHANNEL, bytes)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}