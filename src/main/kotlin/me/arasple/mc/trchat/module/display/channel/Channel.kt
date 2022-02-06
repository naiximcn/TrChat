package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.util.pass
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
class Channel(
    val id: String,
    val settings: ChannelSettings,
    val formats: List<Format>,
    val listeners: MutableList<String>
) {

    fun execute(player: Player, message: String) {
        val tellraw = TellrawJson()
        formats.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix -> tellraw.append(prefix.value.first { it.condition.pass(player) }.content.toTellrawJson(player)) }
            format.msg.serialize(player, message, settings.disabledFunctions)
            format.suffix.forEach { suffix -> tellraw.append(suffix.value.first { it.condition.pass(player) }.content.toTellrawJson(player)) }
        } ?: return

        when (val range = settings.target.range) {

        }

    }

    companion object {

        val channels = mutableListOf<Channel>()
    }
}