package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.IChannel
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.ChatLogs
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.service.Metrics
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers

/**
 * ChannelNormal
 * me.arasple.mc.trchat.common.channel.impl
 *
 * @author wlys
 * @since 2021/8/28 21:03
 */
object ChannelNormal : IChannel {

    override val chatType: ChatType
        get() = ChatType.NORMAL

    override val format: String
        get() = "NORMAL"

    override fun execute(sender: Player, vararg msg: String) {
        val formatted = ChatFormats.getFormat(this, sender)?.apply(sender, msg[0]) ?: return
        onlinePlayers().filterNot { Users.getIgnoredList(it.cast()).contains(sender.name) }.forEach {
            formatted.sendTo(it)
        }
        formatted.sendTo(console())
        ChatLogs.log(sender, msg[0])
        Users.putFormattedMessage(sender, formatted.toRawMessage())
        Metrics.increase(0)
    }
}