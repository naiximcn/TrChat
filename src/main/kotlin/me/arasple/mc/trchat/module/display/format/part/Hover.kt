package me.arasple.mc.trchat.module.display.format.part

import me.arasple.mc.trchat.module.script.Condition
import me.arasple.mc.trmenu.util.Regexs
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/22 9:45
 */
class Hover(text: List<String>, override val condition: Condition?) : Part() {

    override val content = text.joinToString("\n")

    override val dynamic by lazy { Regexs.containsPlaceholder(content) }

    override fun process(tellraw: TellrawJson, player: Player) {
        if (dynamic) {
            tellraw.hoverText(content.replacePlaceholder(player))
        } else {
            tellraw.hoverText(content)
        }
    }
}