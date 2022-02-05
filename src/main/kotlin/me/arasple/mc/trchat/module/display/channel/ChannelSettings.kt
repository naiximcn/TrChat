package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.module.internal.script.Condition

/**
 * @author wlys
 * @since 2022/2/5 13:25
 */
class ChannelSettings(
    val joinCondition: Condition,
    val speakCondition: Condition,
    val target: Channel.Companion.Target,
    val autoJoin: Boolean,
    val proxy: Boolean
)