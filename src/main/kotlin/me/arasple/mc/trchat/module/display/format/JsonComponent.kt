package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.util.pass
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent(
    val text: List<Text>?,
    val hover: Hover?,
    val suggest: List<Suggest>?,
    val command: List<Command>?,
    val url: List<Url>?,
    val insertion: List<Insertion>?,
    val copy: List<Copy>?,
    val font: List<Font>?
) {

    open fun toTextComponent(player: Player, vararg vars: String): TextComponent {
        val builder = text?.firstOrNull { it.condition.pass(player) }?.process(player, *vars) ?: Component.text()

        hover?.process(builder, player, *vars)
        suggest?.firstOrNull { it.condition.pass(player) }?.process(builder, player, *vars)
        command?.firstOrNull { it.condition.pass(player) }?.process(builder, player, *vars)
        url?.firstOrNull { it.condition.pass(player) }?.process(builder, player, *vars)
        insertion?.firstOrNull { it.condition.pass(player) }?.process(builder, player, *vars)
        copy?.firstOrNull { it.condition.pass(player) }?.process(builder, player, *vars)
        font?.firstOrNull { it.condition.pass(player) }?.process(builder, player, *vars)

        return builder.build()
    }
}