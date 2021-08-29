package me.arasple.mc.trchat.api.event

import me.arasple.mc.trchat.common.channel.IChannel
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * TrChatEvent
 * me.arasple.mc.trchat.api.event
 *
 * @author wlys
 * @since 2021/8/20 20:53
 */
class TrChatEvent(val channel: IChannel, val sender: Player, vararg var message: String) : BukkitProxyEvent()