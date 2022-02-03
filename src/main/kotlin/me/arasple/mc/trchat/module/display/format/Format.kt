package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.script.Condition

/**
 * @author wlys
 * @since 2021/12/11 23:27
 */
class Format(
    val condition: Condition?,
    val priority: Int,
    val prefix: List<JsonComponent>,
    val message: MsgComponent,
    val suffix: List<JsonComponent>
)