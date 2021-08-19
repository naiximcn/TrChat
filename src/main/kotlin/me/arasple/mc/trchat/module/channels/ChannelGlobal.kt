package me.arasple.mc.trchat.module.channels

import me.arasple.mc.trchat.Metrics
import me.arasple.mc.trchat.api.event.GlobalShoutEvent
import me.arasple.mc.trchat.module.bungee.Bungees
import me.arasple.mc.trchat.module.chat.ChatFormats
import me.arasple.mc.trchat.module.chat.obj.ChatType
import me.arasple.mc.trchat.module.data.Users
import org.bukkit.entity.Player
import taboolib.common.platform.function.console

/**
 * @author Arasple
 * @date 2019/8/17 23:06
 */
object ChannelGlobal {

    fun execute(from: Player, message: String) {
        GlobalShoutEvent(message).run {
            if (call()) {
                val format = ChatFormats.getFormat(ChatType.GLOBAL, from)!!.apply(from, this.message)
                val raw = format.toRawMessage()
                Bungees.sendBungeeData(from, "TrChat", "BroadcastRaw", raw)
                format.sendTo(console())
                Users.formattedMessage[from.uniqueId] = raw
                Metrics.increase(0)
            }
        }
    }
}