package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.script.Condition
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.module.chat.TellrawJson

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
class Channel(
    val id: String,
    val settings: ChannelSettings,
    val formats: List<Format>,
    val listeners: List<ProxyPlayer>
) {

    fun execute(player: Player, message: String) {
        val tellraw = TellrawJson()
        formats.firstOrNull { it.condition?.eval(player) != false }?.let { format ->
            format.prefix.forEach { tellraw.append(it.toTellrawJson(player)) }
            format.msg.serialize(player, message)
            format.suffix.forEach { tellraw.append(it.toTellrawJson(player)) }
        }


    }

    companion object {

        val channels = mutableListOf<Channel>()

        enum class Range {

            ALL, SINGLE_WORLD, DISTANCE, SELF
        }

        class Target(val range: Range, val distance: Int?)

        class ChannelSettings(
            val joinCondition: Condition,
            val speakCondition: Condition,
            val target: Target,
            val proxy: Boolean
            )
    }
}