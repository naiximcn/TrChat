package me.arasple.mc.trchat.util.proxy.bungee

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.util.proxy.bukkit.Players
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.module.porticus.common.MessageBuilder
import java.io.IOException
import java.util.*

/**
 * @author Arasple
 * @date 2019/8/4 21:23
 */
class 0Bungees : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        val data = ByteStreams.newDataInput(message)
        if (channel == BUNGEE_CHANNEL) {
            try {
                val subChannel = data.readUTF()
                if (subChannel == "PlayerList") {
                    data.readUTF() // server
                    Players.setPlayers(data.readUTF().split(", "))
                }
            } catch (ignored: IOException) {
            }
        }
        if (channel == TRCHAT_CHANNEL) {
            try {
                val subChannel = data.readUTF()
                if (subChannel == "GlobalMute") {
                    when (data.readUTF()) {
                        "on" -> TrChat.isGlobalMuting = true
                        "off" -> TrChat.isGlobalMuting = false
                    }
                }
            } catch (ignored: IOException) {
            }
        }
    }

    companion object {

        const val TRCHAT_CHANNEL = "trchat:main"
        const val BUNGEE_CHANNEL = "BungeeCord"

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, BUNGEE_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChat.plugin, BUNGEE_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, BUNGEE_CHANNEL, Bungees())
            }
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, TRCHAT_CHANNEL)
            }
            if (!Bukkit.getMessenger().isIncomingChannelRegistered(TrChat.plugin, TRCHAT_CHANNEL)) {
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, TRCHAT_CHANNEL, Bungees())
            }
        }

        fun sendBukkitMessage(player: Player, vararg args: String) {
            submit(async = true) {
                try {
                    for (bytes in MessageBuilder.create(arrayOf(UUID.randomUUID().toString(), *args))) {
                        player.sendPluginMessage(TrChat.plugin, TRCHAT_CHANNEL, bytes)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun sendBungeeData(player: Player, vararg args: String) {
            val out = ByteStreams.newDataOutput()

            for (arg in args) {
                try {
                    out.writeUTF(arg)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            player.sendPluginMessage(TrChat.plugin, BUNGEE_CHANNEL, out.toByteArray())
        }
    }
}