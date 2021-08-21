package me.arasple.mc.trchat.api.event

import me.arasple.mc.trchat.common.chat.obj.ChatType
import taboolib.platform.type.BukkitProxyEvent

/**
 * TrChatEvent
 * me.arasple.mc.trchat.api.event
 *
 * @author wlys
 * @since 2021/8/20 20:53
 */
class TrChatEvent(val chatType: ChatType, var message: String) : BukkitProxyEvent()