package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.getSession
import me.arasple.mc.trchat.util.pass
import me.arasple.mc.trchat.util.proxy.sendBukkitMessage
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.platform.util.sendLang
import taboolib.platform.util.toProxyLocation
import java.util.*

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
class Channel(
    val id: String,
    val settings: ChannelSettings,
    val bindings: ChannelBindings,
    val formats: List<Format>,
    val listeners: MutableList<UUID> = mutableListOf()
) {

    fun execute(player: Player, message: String) {
        if (!settings.speakCondition.pass(player)) {
            return
        }
        var builder = Component.text()
        formats.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix -> builder = builder.append(prefix.value.first { it.condition.pass(player) }.content.toTellrawJson(player)) }
            builder = builder.append(format.msg.serialize(player, message, settings.disabledFunctions))
            format.suffix.forEach { suffix -> builder = builder.append(suffix.value.first { it.condition.pass(player) }.content.toTellrawJson(player)) }
        } ?: return
        val component = builder.build()

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
                TrChat.adventure.player(player).sendMessage(Identity.identity(player.uniqueId), component, MessageType.CHAT)
            }
        }

        val gson = BukkitComponentSerializer.gson().serialize(component)

        if (settings.proxy) {
            if (settings.ports != null) {
                player.sendBukkitMessage("ForwardRaw", player.uniqueId.toString(), gson, settings.joinPermission ?: "null", settings.ports.joinToString(";"))
            } else {
                player.sendBukkitMessage("BroadcastRaw", player.uniqueId.toString(), gson, settings.joinPermission ?: "null")
            }
        }

        Metrics.increase(0)
    }

    companion object {

        val channels = mutableListOf<Channel>()

        fun join(player: Player, channel: String) {
            channels.firstOrNull { it.id == channel }?.let {
                join(player, it)
            }
        }

        fun join(player: Player, channel: Channel) {
            player.getSession().channel = channel
            player.sendLang("Channel-Join", channel.id)
        }

        fun quit(player: Player) {
            player.getSession().channel?.id?.let { player.sendLang("Channel-Quit", it) }
            player.getSession().channel = Settings.channelDefault.get()
        }

        private fun ProxyPlayer.toAudience(): Audience {
            return TrChat.adventure.player(cast<Player>())
        }
    }
}