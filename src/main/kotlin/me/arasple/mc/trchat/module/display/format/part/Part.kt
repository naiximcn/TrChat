package me.arasple.mc.trchat.module.display.format.part

import me.arasple.mc.trchat.module.internal.script.Condition
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson

/**
 * @author wlys
 * @since 2022/1/21 23:22
 */
abstract class Part {

    abstract val content: String

    abstract val condition: Condition?

    abstract val dynamic: Boolean

    abstract fun process(tellraw: TellrawJson, player: Player, message: String = ""): String?
}