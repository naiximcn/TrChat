package me.arasple.mc.trchat.common.channel

import me.arasple.mc.trchat.common.chat.obj.ChatType
import org.bukkit.entity.Player

/**
 * Channel
 * me.arasple.mc.trchat.common.channel
 *
 * @author wlys
 * @since 2021/8/28 21:03
 */
abstract class ChannelAbstract {

    abstract val chatType: ChatType

    abstract val format: String

    internal abstract fun execute(sender: Player, vararg msg: String)

}
