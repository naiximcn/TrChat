package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.ChannelAbstract
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.ChatLogs
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.data.Users
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.console
import java.util.*

/**
 * ChannelNormal
 * me.arasple.mc.trchat.common.channel.impl
 *
 * @author wlys
 * @since 2021/8/28 21:03
 */
object ChannelNormal : ChannelAbstract() {

    val targets = mutableMapOf<UUID, List<ProxyPlayer>>()

    override val chatType: ChatType
        get() = ChatType.NORMAL

    override val format: String
        get() = "NORMAL"

    override fun execute(sender: Player, msg: String, args: Array<String>) {
        val formatted = ChatFormats.getFormat(this, sender)?.apply(sender, msg) ?: return
        targets[sender.uniqueId]!!.forEach {
            formatted.sendTo(it)
        }
        formatted.sendTo(console())
        ChatLogs.log(sender, msg)
        Users.putFormattedMessage(sender, formatted.toPlainText())
    }
}