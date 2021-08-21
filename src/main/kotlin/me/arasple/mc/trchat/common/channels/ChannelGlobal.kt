package me.arasple.mc.trchat.common.channels

import me.arasple.mc.trchat.Metrics
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.internal.proxy.bungee.Bungees
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import org.bukkit.entity.Player
import taboolib.common.platform.function.console

/**
 * @author Arasple
 * @date 2019/8/17 23:06
 */
object ChannelGlobal {

    fun execute(from: Player, message: String) {
        TrChatEvent(ChatType.GLOBAL, message).run {
            if (call()) {
                val format = ChatFormats.getFormat(ChatType.GLOBAL, from)!!.apply(from, this.message)
                val raw = format.toRawMessage()
                Bungees.sendBungeeData(from, "TrChat", "BroadcastRaw", raw)
                format.sendTo(console())
                Metrics.increase(0)
            }
        }
    }
}