package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.IChannel
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.proxy.Proxy
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
object ChannelPrivateReceive : IChannel {

    override val chatType: ChatType
        get() = ChatType.PRIVATE_RECEIVE

    override val format: String
        get() = "PRIVATE_RECEIVE"

    override fun execute(sender: Player, vararg msg: String) {
        val formatted = ChatFormats.getFormat(this, sender)!!.apply(sender, msg[0], "true", msg[1])

        val toPlayer = Bukkit.getPlayerExact(msg[1])
        if (toPlayer == null || !toPlayer.isOnline) {
            val raw = formatted.toRawMessage()
            Proxy.sendProxyData(sender, "SendRaw", msg[1], raw)
        } else {
            formatted.sendTo(getProxyPlayer(msg[1])!!)
            getProxyPlayer(msg[1])!!.sendLang("Private-Message-Receive", sender.name)
        }
    }
}