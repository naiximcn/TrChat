package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.display.format.part.*
import me.arasple.mc.trchat.module.internal.script.Condition
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent(
    val condition: Condition?,
    val text: List<Text>?,
    val hover: List<Hover>?,
    val suggest: List<Suggest>?,
    val command: List<Command>?,
    val url: List<Url>?,
    val insertion: List<Insertion>?,
    val copy: List<Copy>?
) {

    open fun toTellrawJson(player: Player, vararg vars: String): TellrawJson {
        val tellraw = TellrawJson()
        if (condition?.eval(player) == false) {
            return tellraw
        }

        text!!.firstOrNull { it.condition?.eval(player) != false }?.process(tellraw, player)
        hover?.firstOrNull { it.condition?.eval(player) != false }?.process(tellraw, player)
        suggest?.firstOrNull { it.condition?.eval(player) != false }?.process(tellraw, player)
        command?.firstOrNull { it.condition?.eval(player) != false }?.process(tellraw, player)
        url?.firstOrNull { it.condition?.eval(player) != false }?.process(tellraw, player)
        insertion?.firstOrNull { it.condition?.eval(player) != false }?.process(tellraw, player)
        copy?.firstOrNull { it.condition?.eval(player) != false }?.process(tellraw, player)

        return tellraw
    }
}