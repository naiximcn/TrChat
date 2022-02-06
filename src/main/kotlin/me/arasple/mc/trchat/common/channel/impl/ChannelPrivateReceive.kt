package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.module.internal.data.ChatLogs
import me.arasple.mc.trchat.module.internal.command.main.CommandReply
import me.arasple.mc.trchat.util.proxy.sendBukkitMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang
import java.util.*

/**
 * ChannelPrivateReceive
 * me.arasple.mc.trchat.common.channel.impl
 *
 * @author wlys
 * @since 2021/8/28 21:17
 */
object ChannelPrivateReceive {

    override fun execute(sender: Player, msg: String, args: Array<String>) {
        val formatted = ChatFormats.getFormat(this, sender)?.apply(sender, msg, "true", args[0], privateChat = true) ?: return

        val toPlayer = Bukkit.getPlayerExact(args[0])
        if (toPlayer == null || !toPlayer.isOnline) {
            val raw = formatted.toRawMessage()
            sender.sendBukkitMessage("SendRaw", args[0], raw)
        } else {
            getProxyPlayer(args[0])?.let {
                formatted.sendTo(it)
                it.sendLang("Private-Message-Receive", sender.name)
            }
        }

        // Spy
        spying.forEach {
            val spyPlayer = getProxyPlayer(it)
            if (spyPlayer != null && spyPlayer.isOnline()) {
                spyPlayer.sendLang("Private-Message-Spy-Format", sender.name, args[0], msg)
            }
        }
        console().sendLang("Private-Message-Spy-Format", sender.name, args[0], msg)

        ChatLogs.logPrivate(sender.name, args[0], msg)
        CommandReply.lastMessageFrom[args[0]] = sender.name
    }
}