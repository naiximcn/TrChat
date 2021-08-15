package me.arasple.mc.trchat.api.event

import taboolib.platform.type.BukkitProxyEvent

/**
 * GlobalChatEvent
 * me.arasple.mc.trchat.api.event
 *
 * @author wlys
 * @since 2021/8/11 20:00
 */
class GlobalShoutEvent(var message: String) : BukkitProxyEvent()