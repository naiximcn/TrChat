package me.arasple.mc.trchat.common.chat.format

import org.bukkit.entity.Player
import taboolib.common5.mirrorNow
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2019/12/1 11:42
 */
class PriFormat(formatMap: Map<*, *>) : Format(formatMap) {

    init {
        msg.isPrivateChat = true
    }

    override fun apply(player: Player, vararg message: String, post: Boolean): TellrawJson {
        return mirrorNow("Common:Format:private") {
            val format = TellrawJson()
            jsons.forEach { x -> format.append(x.toTellrawJson(player, false, "true", message[1], message[2])) }
            format.append(msg.toTellrawJson(player, message[0]))
            return@mirrorNow format
        }
    }
}