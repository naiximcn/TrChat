package me.arasple.mc.trchat.common.channel.impl

import me.arasple.mc.trchat.common.channel.IChannel
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.proxy.Proxy
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.library.configuration.MemorySection
import taboolib.platform.util.sendLang
import taboolib.platform.util.toProxyLocation

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
    val target: Target,
    val alwaysReceive: Boolean,
    val private: Boolean,
    val forwardToDynmap: Boolean,
    val hint: Boolean,
    val sendToConsole: Boolean,
    val autoQuit: Boolean
    ): IChannel {

    constructor(name: String, obj: MemorySection) : this(
        name,
        obj.getString("FORMAT"),
        obj.getString("PERMISSION"),
        obj.getString("RANGE", "ALL").split(";").let {
            Target(Range.valueOf(it[0].uppercase()), it.getOrNull(1)?.toIntOrNull())
        },
        obj.getBoolean("PRIVATE", false),
        obj.getBoolean("ALWAYS-RECEIVE", false),
        obj.getBoolean("FORWARD-TO-DYNMAP", false),
        obj.getBoolean("HINT", true),
        obj.getBoolean("SEND-TO-CONSOLE", true),
        obj.getBoolean("AUTO-QUIT", false)
        )

    override val chatType: ChatType
        get() = ChatType.CUSTOM

    override fun execute(sender: Player, vararg msg: String) {
        val formatted = ChatFormats.getFormat(this, sender)!!.apply(sender, msg[0], forwardToDynmap = forwardToDynmap, privateChat = private)
        if (alwaysReceive) {
            if (Proxy.isEnabled) {
                Proxy.sendProxyData(sender, "SendRawPerm", formatted.toRawMessage(), permission)
            } else {
                onlinePlayers().filter { it.hasPermission(permission) }.forEach {
                    formatted.sendTo(it)
                }
            }
        } else {
            when(target.range) {
                Range.ALL -> {
                    onlinePlayers().filter { Users.getCustomChannel(it.cast()) == this }.forEach {
                        formatted.sendTo(it)
                    }
                }
                Range.SINGLE_WORLD -> {
                    onlinePlayers().filter { Users.getCustomChannel(it.cast()) == this && it.world == sender.world.name }.forEach {
                        formatted.sendTo(it)
                    }
                }
                Range.DISTANCE -> {
                    onlinePlayers().filter { Users.getCustomChannel(it.cast()) == this
                            && it.world == sender.world.name
                            && it.location.distance(sender.location.toProxyLocation()) <= target.distance!! }.forEach {
                        formatted.sendTo(it)
                    }
                }
                Range.SELF -> {
                    formatted.sendTo(adaptPlayer(sender))
                }
            }
        }
        if (sendToConsole) {
            formatted.sendTo(console())
        }
    }

    override fun toString(): String {
        return name
    }

    enum class Range {

        ALL, SINGLE_WORLD, DISTANCE, SELF
    }

    class Target(val range: Range, val distance: Int?)

    companion object {

        val list = mutableListOf<ChannelCustom>()

        fun of(channel: String?): ChannelCustom? {
            return list.firstOrNull { it.name == (channel ?: return null) }
        }

        fun join(player: Player, channel: String) {
            join(player, of(channel) ?: return)
        }

        fun join(player: Player, cc: ChannelCustom) {
            Users.removeCustomChannel(player)
            Users.setCustomChannel(player, cc)
            if (cc.hint) {
                player.sendLang("Custom-Channel-Join", cc.name)
            }
        }

        @SubscribeEvent
        private fun e(e: PlayerQuitEvent) {
            val player = e.player
            val channel = Users.getCustomChannel(player) ?: return

            if (channel.autoQuit) {
                Users.setCustomChannel(player, null)
            }
        }
    }
}