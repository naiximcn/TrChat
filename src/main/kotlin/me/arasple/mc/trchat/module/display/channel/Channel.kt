package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.module.display.channel.obj.ChannelBindings
import me.arasple.mc.trchat.module.display.channel.obj.ChannelSettings
import me.arasple.mc.trchat.module.display.channel.obj.Target
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.data.ChatLogs
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.checkMute
import me.arasple.mc.trchat.util.getSession
import me.arasple.mc.trchat.util.pass
import me.arasple.mc.trchat.util.proxy.sendBukkitMessage
import me.arasple.mc.trchat.util.toAudience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import taboolib.common.platform.command.command
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.subList
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import taboolib.platform.util.toProxyLocation
import java.util.*

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
open class Channel(
    val id: String,
    val settings: ChannelSettings,
    val bindings: ChannelBindings,
    val formats: List<Format>,
) {

    init {
        if (!bindings.command.isNullOrEmpty()) {
            command(bindings.command[0], subList(bindings.command, 1), "Channel $id speak command") {
                execute<Player> { sender, _, _ ->
                    join(sender, this@Channel)
                }
                dynamic("message") {
                    execute<Player> { sender, _, argument ->
                        execute(sender, argument)
                    }
                }
                incorrectSender { sender, _ ->
                    sender.sendLang("Command-Not-Player")
                }
            }
        }
    }

    val listeners = mutableListOf<UUID>()

    open fun execute(player: Player, message: String) {
        if (!player.checkMute()) {
            return
        }
        if (!settings.speakCondition.pass(player)) {
            player.sendLang("Channel-No-Speak-Permission")
            return
        }
        val event = TrChatEvent(this, player.getSession(), message)
        if (!event.call()) {
            return
        }
        val msg = event.message

        val builder = Component.text()
        formats.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix ->
                builder.append(prefix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
            builder.append(format.msg.serialize(player, msg, settings.disabledFunctions))
            format.suffix.forEach { suffix ->
                builder.append(suffix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
        } ?: return
        val component = builder.build()

        if (settings.proxy) {
            val gson = BukkitComponentSerializer.gson().serialize(component)
            if (settings.ports != null) {
                player.sendBukkitMessage(
                    "ForwardRaw",
                    player.uniqueId.toString(),
                    gson,
                    settings.joinPermission ?: "null",
                    settings.ports.joinToString(";")
                )
            } else {
                player.sendBukkitMessage(
                    "BroadcastRaw",
                    player.uniqueId.toString(),
                    gson,
                    settings.joinPermission ?: "null"
                )
            }
            return
        }
        when (settings.target.range) {
            Target.Range.ALL -> {
                listeners.forEach {
                    TrChat.adventure.player(it).sendMessage(Identity.identity(player.uniqueId), component, MessageType.CHAT)
                }
            }
            Target.Range.SINGLE_WORLD -> {
                onlinePlayers().filter { listeners.contains(it.uniqueId) && it.world == player.world.name }.forEach {
                    it.toAudience().sendMessage(Identity.identity(player.uniqueId), component, MessageType.CHAT)
                }
            }
            Target.Range.DISTANCE -> {
                onlinePlayers().filter { listeners.contains(it.uniqueId)
                        && it.world == player.world.name
                        && it.location.distance(player.location.toProxyLocation()) <= settings.target.distance }.forEach {
                    it.toAudience().sendMessage(Identity.identity(player.uniqueId), component, MessageType.CHAT)
                }
            }
            Target.Range.SELF -> {
                player.toAudience().sendMessage(Identity.identity(player.uniqueId), component, MessageType.CHAT)
            }
        }

        player.getSession().lastMessage = message
        ChatLogs.log(player, message)
        Metrics.increase(0)
    }

    companion object {

        val channels = mutableListOf<Channel>()

        val defaultChannel by lazy {
            channels.first { it.id == Settings.CONF.getString("Channel.Default") }
        }

        fun join(player: Player, channel: String) {
            channels.firstOrNull { it.id == channel }?.let {
                join(player, it)
            }
        }

        fun join(player: Player, channel: Channel) {
            if (channel.settings.joinPermission?.let { player.hasPermission(it) } == false) {
                player.sendLang("General-No-Permission")
                return
            }
            player.getSession().channel = channel
            if (!channel.settings.autoJoin) {
                channel.listeners.add(player.uniqueId)
            }
            player.sendLang("Channel-Join", channel.id)
        }

        fun quit(player: Player) {
            player.getSession().channel?.let {
                if (!it.settings.autoJoin) {
                    it.listeners.remove(player.uniqueId)
                }
                player.sendLang("Channel-Quit", it.id)
            }
            player.getSession().channel = defaultChannel
        }
    }
}