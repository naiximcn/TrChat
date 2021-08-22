package me.arasple.mc.trchat.internal.proxy.bungee

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.internal.proxy.Proxy
import me.arasple.mc.trchat.internal.proxy.bukkit.Players.setPlayers
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import taboolib.common.platform.function.console
import taboolib.common.platform.function.server
import taboolib.module.lang.sendLang
import java.io.IOException

/**
 * @author Arasple
 * @date 2019/8/4 21:23
 */
class Bungees : PluginMessageListener {

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if (channel != "BungeeCord") {
            return
        }
        val data = ByteStreams.newDataInput(message)
        try {
            val subChannel = data.readUTF()
            if (subChannel == "PlayerList") {
                val server = data.readUTF()
                setPlayers(data.readUTF().split(", "))
            }
        } catch (ignored: IOException) {
        }
    }

    companion object {

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, "BungeeCord")) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, "BungeeCord")
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, "BungeeCord", Bungees())
                Proxy.isEnabled = Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord", false)
            }
        }

        fun sendBungeeData(player: Player, vararg args: String) {
            val out = ByteStreams.newDataOutput()
            try {
                out.writeUTF("TrChat")
            } catch (e: IOException) {
                e.printStackTrace()
            }
            for (arg in args) {
                try {
                    out.writeUTF(arg)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            player.sendPluginMessage(TrChat.plugin, "BungeeCord", out.toByteArray())
        }

        fun sendBungeeData(vararg args: String) {
            val out = ByteStreams.newDataOutput()
            for (arg in args) {
                try {
                    out.writeUTF(arg)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            server<Server>().sendPluginMessage(TrChat.plugin, "BungeeCord", out.toByteArray())
        }
    }
}