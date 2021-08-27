package me.arasple.mc.trchat.internal.proxy.velocity

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.internal.proxy.Proxy
import me.arasple.mc.trchat.internal.proxy.bukkit.Players
import me.arasple.mc.trchat.internal.proxy.bungee.Bungees
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.IOException

/**
 * Velocity
 * me.arasple.mc.trchat.internal.proxy.velocity
 *
 * @author wlys
 * @since 2021/8/21 19:15
 */
class Velocity : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != "trchat:server") {
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

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, "trchat:proxy")) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, "trchat:proxy")
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, "trchat:server", Velocity())
                Proxy.isEnabled = !Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord", false)
            }
        }

        fun sendVelocityData(player: Player, vararg args: String) {
            val out = ByteStreams.newDataOutput()
            for (arg in args) {
                try {
                    out.writeUTF(arg)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            player.sendPluginMessage(TrChat.plugin, "trchat:proxy", out.toByteArray())
        }
    }
}