package me.arasple.mc.trchat.common.channels

import me.arasple.mc.trchat.internal.service.Metrics
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.proxy.Proxy
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.platform.util.sendLang

/**
 * @author Arasple
 * @date 2019/8/17 23:06
 */
object ChannelGlobal {

    fun execute(from: Player, message: String) {
        if (!Proxy.isEnabled) {
            from.sendLang("Global-Message-Not-Enable")
            return
        }
        TrChatEvent(ChatType.GLOBAL, message).run {
            if (call()) {
                val format = ChatFormats.getFormat(ChatType.GLOBAL, from)!!.apply(from, this.message)
                val raw = format.toRawMessage()
                Proxy.sendProxyData(from, "BroadcastRaw", raw)
                format.sendTo(console())
                Metrics.increase(0)
            }
        }
    }
}