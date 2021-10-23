package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.ChannelAbstract
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer

/**
 * ChannelPrivateSend
 * me.arasple.mc.trchat.common.channel.impl
 *
 * @author wlys
 * @since 2021/8/28 21:16
 */
object ChannelPrivateSend : ChannelAbstract() {

    override val chatType: ChatType
        get() = ChatType.PRIVATE_SEND

    override val format: String
        get() = "PRIVATE_SEND"

    override fun execute(sender: Player, msg: String, args: Array<String>) {
        val formatted = ChatFormats.getFormat(this, sender)?.apply(sender, msg, "true", args[0], privateChat = true) ?: return
        formatted.sendTo(adaptPlayer(sender))
    }
}