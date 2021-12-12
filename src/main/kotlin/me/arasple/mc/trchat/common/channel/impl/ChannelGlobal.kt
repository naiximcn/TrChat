package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.ChannelAbstract
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.module.data.ChatLogs
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.proxy.Proxy
import me.arasple.mc.trchat.internal.proxy.sendBukkitMessage
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.platform.util.sendLang

/**
 * @author Arasple
 * @date 2019/8/17 23:06
 */
object ChannelGlobal : ChannelAbstract() {

    override val chatType: ChatType
        get() = ChatType.GLOBAL

    override val format: String
        get() = "GLOBAL"

    override fun execute(sender: Player, msg: String, args: Array<String>) {
        if (!Proxy.isEnabled) {
            sender.sendLang("Global-Message-Not-Enable")
            return
        }
        val formatted = ChatFormats.getFormat(this, sender)?.apply(sender, msg) ?: return
        val raw = formatted.toRawMessage()
        sender.sendBukkitMessage("BroadcastRaw", sender.uniqueId.toString(), raw)
        formatted.sendTo(console())
        ChatLogs.log(sender, msg)
        Users.putFormattedMessage(sender, formatted.toPlainText())
    }
}