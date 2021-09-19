package me.arasple.mc.trchat.common.chat

import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
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
class ChatMessage(
    val packet: Packet,
    val message: String?,
) {

    companion object {

        val MESSAGES = ConcurrentHashMap<UUID, ArrayList<ChatMessage>>()

        fun addMessage(player: Player, packet: Packet) {
            val message = MESSAGES.computeIfAbsent(player.uniqueId) { ArrayList() }
            message.add(ChatMessage(packet, packetToMessage(packet)))
            if (message.size > 64) {
                message.removeFirstOrNull()
            }
        }

        fun removeMessage(message: String) {
            MESSAGES.entries.forEach { (_, v) ->
                v.removeIf { it.message == message }
            }
        }

        fun releaseMessage() {
            MESSAGES.forEach { (k, v) ->
                val player = Bukkit.getPlayer(k) ?: return
                v.forEach {
                    player.sendPacket(it.packet)
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
    }
}