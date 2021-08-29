package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.IChannel
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.proxy.Proxy
import me.arasple.mc.trchat.internal.service.Metrics
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.library.configuration.MemorySection

/**
 * ChannelCustom
 * me.arasple.mc.trchat.common.channel.impl
 *
 * @author wlys
 * @since 2021/8/28 21:18
 */
class ChannelCustom(
    val name: String,
    override val format: String,
    val permission: String,
    val private: Boolean,
    val isAlwaysReceive: Boolean,
    val isForwardToDynmap: Boolean,
    val isHint: Boolean,
    val isSelfVisible: Boolean,
    val isSendToConsole: Boolean
    ): IChannel {

    constructor(name: String, obj: MemorySection) : this(
        name,
        obj.getString("FORMAT"),
        obj.getString("PERMISSION"),
        obj.getBoolean("PRIVATE", false),
        obj.getBoolean("ALWAYS-RECEIVE", false),
        obj.getBoolean("FORWARD-TO-DYNMAP", false),
        obj.getBoolean("HINT", true),
        obj.getBoolean("SELF-VISIBLE", false),
        obj.getBoolean("SEND-TO-CONSOLE", true)
    )

    init {
        if (private) {
            ChatFormats.formats[this.format]?.forEach {
                it.msg.isPrivateChat = true
            }
        }
    }

    override val chatType: ChatType
        get() = ChatType.CUSTOM

    override fun execute(sender: Player, vararg msg: String) {
        val formatted = ChatFormats.getFormat(this, sender)!!.apply(sender, msg[0], forwardToDynmap = isForwardToDynmap)
        if (!isSelfVisible) {
            if (isAlwaysReceive) {
                if (Proxy.isEnabled) {
                    Proxy.sendProxyData(sender, "SendRawPerm", formatted.toRawMessage(), permission)
                } else {
                    onlinePlayers().filter { it.hasPermission(permission) }.forEach {
                        formatted.sendTo(it)
                    }
                }
            } else {
                onlinePlayers().filter { Users.getCustomChannel(it.cast()) == this }.forEach {
                    formatted.sendTo(it)
                }
            }
        } else {
            formatted.sendTo(adaptPlayer(sender))
        }
        if (isSendToConsole) {
            formatted.sendTo(console())
        }
        Metrics.increase(0)
    }

    companion object {

        val list = mutableListOf<ChannelCustom>()
    }
}