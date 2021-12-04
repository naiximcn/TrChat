package me.arasple.mc.trchat.common.chat

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.common.channel.ChannelAbstract
import me.arasple.mc.trchat.common.chat.format.Format
import me.arasple.mc.trchat.internal.script.Condition
import me.arasple.mc.trchat.util.notify
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import kotlin.system.measureTimeMillis

/**
 * @author Arasple, wlys
 * @date 2019/11/30 12:09
 */
object ChatFormats {

    val formats = HashMap<String, List<Format>>()

    fun getFormat(channel: ChannelAbstract, player: Player): Format? {
        return formats.computeIfAbsent(channel.format) { ArrayList() }.firstOrNull { format ->
            Condition.eval(player, format.requirement).asBoolean()
        }
    }

    fun loadFormats(vararg notify: ProxyCommandSender) {
        measureTimeMillis {
            formats.entries.clear()

            TrChatFiles.formats.getKeys(false).forEach { format ->
                formats[format] = TrChatFiles.formats.getMapList(format).map { Format(it) }.sortedBy { it.priority }
            }
        }.also { notify(notify, "Plugin-Loaded-Chat-Formats", it) }
    }
}