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
interface IChannel {

    val chatType: ChatType

    val format: String

    fun execute(sender: Player, vararg msg: String)

}
