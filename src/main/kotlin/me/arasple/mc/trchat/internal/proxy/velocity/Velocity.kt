package me.arasple.mc.trchat.internal.proxy.velocity

import com.google.common.io.ByteStreams
import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.internal.proxy.Proxy
import me.arasple.mc.trchat.internal.proxy.bukkit.Players
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import taboolib.module.porticus.common.MessageBuilder
import java.io.IOException
import java.util.*

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
        } catch (_: IOException) {
        }
    }

    companion object {

        fun init() {
            if (!Bukkit.getMessenger().isOutgoingChannelRegistered(TrChat.plugin, "trchat:proxy")) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(TrChat.plugin, "trchat:proxy")
                Bukkit.getMessenger().registerIncomingPluginChannel(TrChat.plugin, "trchat:server", Velocity())
            }
            Proxy.isEnabled = !Bukkit.getServer().spigot().config.getBoolean("settings.bungeecord", false).also {
                if (it) {
                    console().sendLang("Plugin-Proxy-Supported", "Velocity")
                } else {
                    console().sendLang("Plugin-Proxy-None")
                }
            }
        }

        fun sendBukkitMessage(player: Player, vararg args: String) {
            submit(async = true) {
                try {
                    for (bytes in MessageBuilder.create(arrayOf(UUID.randomUUID().toString(), *args))) {
                        player.sendPluginMessage(TrChat.plugin, "trchat:proxy", bytes)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}