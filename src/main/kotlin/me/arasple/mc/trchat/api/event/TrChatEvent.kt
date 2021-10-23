package me.arasple.mc.trchat.api.event

import me.arasple.mc.trchat.common.channel.ChannelAbstract
import me.arasple.mc.trchat.util.filterUUID
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * TrChatEvent
 * me.arasple.mc.trchat.api.event
 *
 * @author wlys
 * @since 2021/8/20 20:53
 */
class TrChatEvent(val channel: ChannelAbstract, val sender: Player, var message: String, val args: Array<String> = emptyArray()) : BukkitProxyEvent() {

    init {
        message = message.filterUUID().replace("<", "\\<")
    }
}