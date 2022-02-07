package me.arasple.mc.trchat.module.display.format.part.json

import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.Regexs
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.legacy
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/21 23:21
 */
class Text(val content: String, val condition: Condition?) {

    val dynamic by lazy { Regexs.containsPlaceholder(content) }

    fun process(player: Player, vararg vars: String, message: String = ""): TextComponent {
        val text = if (dynamic) {
            content.replacePlaceholder(player).replace("\$message", message).replaceWithOrder(*vars).colorify()
        } else {
            content.replace("\$message", message).replaceWithOrder(*vars).colorify()
        }
        return legacy(text)
    }
}