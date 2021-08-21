package me.arasple.mc.trchat.internal.proxy.bungee

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.internal.proxy.Players.setPlayers
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import taboolib.common.platform.function.console
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

        var isEnabled = false

        fun init(plugin: JavaPlugin) {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(plugin, "BungeeCord")) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord")
                Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", Bungees())
                isEnabled = Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord", false)
                console().sendLang(if (isEnabled) "Plugin-Proxy-Bungee" else "Plugin-Proxy-None")
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