package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.module.display.format.Format
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
class Channel(
    val id: String,
    val formats: List<Format>
) {

    fun execute(player: Player, message: String) {
        TellrawJson()
        formats.firstOrNull { it.condition?.eval(player) != false }
    }

    companion object {

        val channels = mutableListOf<Channel>()
    }
}