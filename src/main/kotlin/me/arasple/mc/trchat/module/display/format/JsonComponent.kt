package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.util.pass
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent(
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

        text!!.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, *vars)
        hover?.filter { it.condition.pass(player) }?.joinToString("\n") { it.process(tellraw, player, *vars) }?.let { tellraw.hoverText(it) }
        suggest?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, *vars)
        command?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, *vars)
        url?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, *vars)
        insertion?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, *vars)
        copy?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, *vars)

        return tellraw
    }
}