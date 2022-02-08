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
    val copy: List<Copy>?
) {

    open fun toTextComponent(player: Player, vararg vars: String): TextComponent {
        var component = text?.firstOrNull { it.condition.pass(player) }?.process(player, *vars) ?: return Component.empty()

        component = hover?.process(component, player, *vars) ?: component
        component = suggest?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = command?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = url?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = insertion?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = copy?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component

        return component
    }
}