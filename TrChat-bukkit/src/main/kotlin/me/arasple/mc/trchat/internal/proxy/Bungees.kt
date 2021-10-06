package me.arasple.mc.trchat.internal.proxy

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.internal.proxy.Players.setPlayers
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import taboolib.common.platform.function.submit
import taboolib.module.porticus.common.MessageBuilder
import java.io.IOException
import java.util.*

/**
 * @author Arasple
 * @date 2019/8/4 21:23
 */
class Bungees : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        val data = ByteStreams.newDataInput(message)
        if (channel == "BungeeCord") {
            try {
                val subChannel = data.readUTF()
                if (subChannel == "PlayerList") {
                    data.readUTF() // server
                    setPlayers(data.readUTF().split(", "))
                }
            } catch (ignored: IOException) {
            }
        }
        if (channel == "trchat:main") {
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

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, "BungeeCord")) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, "BungeeCord")
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, "BungeeCord", Bungees())
            }
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, "trchat:main")) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, "trchat:main")
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, "trchat:main", Bungees())
            }
            Proxy.isEnabled = Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord", false)
        }

        fun sendBukkitMessage(player: Player, vararg args: String) {
            submit(async = true) {
                try {
                    for (bytes in MessageBuilder.create(arrayOf(UUID.randomUUID().toString(), *args))) {
                        player.sendPluginMessage(TrChat.plugin, "trchat:main", bytes)
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
            player.sendPluginMessage(TrChat.plugin, "BungeeCord", out.toByteArray())
        }
    }
}