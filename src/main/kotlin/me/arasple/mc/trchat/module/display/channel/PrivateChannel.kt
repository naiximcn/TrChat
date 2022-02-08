package me.arasple.mc.trchat.module.display.channel

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.internal.command.main.CommandReply
import me.arasple.mc.trchat.module.internal.data.ChatLogs
import me.arasple.mc.trchat.module.internal.service.Metrics
import me.arasple.mc.trchat.util.getSession
import me.arasple.mc.trchat.util.pass
import me.arasple.mc.trchat.util.proxy.Proxy
import me.arasple.mc.trchat.util.proxy.sendBukkitMessage
import me.arasple.mc.trchat.util.proxy.sendProxyLang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2022/2/8 11:03
 */
class PrivateChannel(
    id: String,
    settings: ChannelSettings,
    bindings: ChannelBindings,
    val sender: List<Format>,
    val receiver: List<Format>
) : Channel(id, settings, bindings, emptyList()) {

    override fun execute(player: Player, message: String) {
        var builder = Component.text()
        sender.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix ->
                builder = builder.append(prefix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
            builder = builder.append(format.msg.serialize(player, message, settings.disabledFunctions))
            format.suffix.forEach { suffix ->
                builder = builder.append(suffix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
        } ?: return
        val send = builder.build()

        builder = Component.text()
        receiver.firstOrNull { it.condition.pass(player) }?.let { format ->
            format.prefix.forEach { prefix ->
                builder = builder.append(prefix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
            builder = builder.append(format.msg.serialize(player, message, settings.disabledFunctions))
            format.suffix.forEach { suffix ->
                builder = builder.append(suffix.value.first { it.condition.pass(player) }.content.toTextComponent(player)) }
        } ?: return
        val receive = builder.build()

        TrChat.adventure.player(player).sendMessage(send)

        val session = player.getSession()

        if (settings.proxy && Proxy.isEnabled) {
            player.sendBukkitMessage(
                "SendRaw",
                session.lastPrivateTo,
                GsonComponentSerializer.gson().serialize(receive)
            )
            player.sendProxyLang("Private-Message-Receive", player.name)
        } else {
            getProxyPlayer(player.getSession().lastPrivateTo)?.let {
                it.toAudience().sendMessage(receive)
                it.sendLang("Private-Message-Receive", player.name)
            }
        }

        ChatSession.SESSIONS.filterValues { it.isSpying }.entries.forEach { (_, v) ->
            v.player.sendLang("Private-Message-Spy-Format", player.name, session.lastPrivateTo, message)
        }
        console().sendLang("Private-Message-Spy-Format", player.name, session.lastPrivateTo, message)

        CommandReply.lastMessageFrom[session.lastPrivateTo] = player.name
        ChatLogs.logPrivate(player.name, session.lastPrivateTo, message)
        Metrics.increase(0)
    }
}