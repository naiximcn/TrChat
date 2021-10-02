package me.arasple.mc.trchat.common.chat

import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.nms.Packet
import taboolib.module.nms.PacketSendEvent
import taboolib.module.nms.sendPacket
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ChatMessage
 * me.arasple.mc.trchat.common.chat
 *
 * @author wlys
 * @since 2021/9/11 19:46
 */
@PlatformSide([Platform.BUKKIT])
object ChatMessage {

    val MESSAGES = ConcurrentHashMap<UUID, MutableList<ChatMessage>>()

    private fun addMessage(player: Player, packet: Packet) {
        val message = MESSAGES.computeIfAbsent(player.uniqueId) { ArrayList() }
        message += ChatMessage(packet.source, packetToMessage(packet)?.replace("\\s".toRegex(), "")?.takeLast(32))
        if (message.size > 100) {
            message.removeFirstOrNull()
        }
    }

    fun removeMessage(message: String) {
        MESSAGES.entries.forEach { (_, v) ->
            v.removeAll { it.message == message }
        }
    }

    fun releaseMessage() {
        val messages = HashMap(MESSAGES)
        MESSAGES.clear()
        onlinePlayers().forEach { player ->
            repeat(100) { player.sendMessage("") }
            messages.remove(player.uniqueId)?.forEach {
                player.cast<Player>().sendPacket(it.packet)
            }
        }
    }

    private fun packetToMessage(packet: Packet): String? {
        return kotlin.runCatching {
            BaseComponent.toPlainText(*packet.read<Array<BaseComponent>>("components")!!)
        }.getOrNull()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private fun e(e: PacketSendEvent) {
        if (e.packet.name == "PacketPlayOutChat") {
            addMessage(e.player, e.packet)
        }
    }

    @SubscribeEvent
    private fun e(e: PlayerQuitEvent) {
        MESSAGES.remove(e.player.uniqueId)
    }

    class ChatMessage(val packet: Any, val message: String?)
}