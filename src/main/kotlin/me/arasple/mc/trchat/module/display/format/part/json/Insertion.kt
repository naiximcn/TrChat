package me.arasple.mc.trchat.module.display.format.part.json

import me.arasple.mc.trchat.module.display.format.part.Part
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.Regexs
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/21 23:21
 */
class Insertion(override val content: String, override val condition: Condition?) : Part() {

    override val dynamic by lazy { Regexs.containsPlaceholder(content) }

    override fun process(tellraw: TellrawJson, player: Player, message: String): String? {
        if (dynamic) {
            tellraw.insertion(content.replacePlaceholder(player).replace("\$message", message))
        } else {
            tellraw.insertion(content.replace("\$message", message))
        }
        return null
    }
}