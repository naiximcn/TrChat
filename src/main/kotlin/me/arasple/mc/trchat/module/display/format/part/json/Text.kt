package me.arasple.mc.trchat.module.display.format.part.json

import me.arasple.mc.trchat.module.display.format.part.Part
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.Regexs
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/21 23:21
 */
class Text(override val content: String, override val condition: Condition?) : Part() {

    override val dynamic by lazy { Regexs.containsPlaceholder(content) }

    override fun process(tellraw: TellrawJson, player: Player, vararg vars: String, message: String): String? {
        if (dynamic) {
            tellraw.append(content.replacePlaceholder(player).replace("\$message", message).replaceWithOrder(*vars).colorify())
        } else {
            tellraw.append(content.replace("\$message", message).replaceWithOrder(*vars).colorify())
        }
        return null
    }
}