package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.ChannelAbstract
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.proxy.sendBukkitMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang

/**
 * ChannelPrivateReceive
 * me.arasple.mc.trchat.common.channel.impl
 *
 * @author wlys
 * @since 2021/8/28 21:17
 */
object ChannelPrivateReceive : ChannelAbstract() {

    override val chatType: ChatType
        get() = ChatType.PRIVATE_RECEIVE

    override val format: String
        get() = "PRIVATE_RECEIVE"

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
    }
}