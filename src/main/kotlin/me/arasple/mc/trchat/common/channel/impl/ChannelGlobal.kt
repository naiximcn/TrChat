package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.internal.service.Metrics
import me.arasple.mc.trchat.common.channel.IChannel
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
object ChannelGlobal : IChannel {

    override val chatType: ChatType
        get() = ChatType.GLOBAL

    override val format: String
        get() = "GLOBAL"

    override fun execute(sender: Player, vararg msg: String) {
        if (!Proxy.isEnabled) {
            sender.sendLang("Global-Message-Not-Enable")
            return
        }
        val format = ChatFormats.getFormat(this, sender)!!.apply(sender, msg[0])
        val raw = format.toRawMessage()
        Proxy.sendProxyData(sender, "BroadcastRaw", raw)
        format.sendTo(console())
        Metrics.increase(0)
    }
}