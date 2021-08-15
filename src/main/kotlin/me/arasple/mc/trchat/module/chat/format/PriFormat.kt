package me.arasple.mc.trchat.module.chat.format

import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2019/12/1 11:42
 */
class PriFormat(formatMap: Map<*, *>) : Format(formatMap) {

    override fun apply(player: Player, vararg message: String): TellrawJson {
        val format = TellrawJson()
        jsons.forEach { x -> format.append(x.toTellrawJson(player, false, "true", message[1], message[2])) }
        format.append(msg.toTellrawJson(player, message[0]))
        return format
    }
}