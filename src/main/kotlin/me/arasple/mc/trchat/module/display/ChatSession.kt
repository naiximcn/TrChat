package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.api.Settings
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.entity.Player
import taboolib.common.platform.function.onlinePlayers
import taboolib.expansion.getDataContainer
import taboolib.module.nms.Packet
import taboolib.module.nms.sendPacket
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author wlys
 * @since 2021/12/11 22:44
 */
class ChatSession(
    val player: Player,
    var channel: Channel?,
    var recipients: Set<Player>
) {

    var lastMessage = ""

    var lastPrivateFrom: UUID? = null

    private val receivedMessages = mutableListOf<ChatMessage>()

    val isFilterEnabled get() = player.getDataContainer()["filter"].toBoolean()

    val isMuted get() = (player.getDataContainer()["mute_time"]?.toLongOrNull() ?: 0) > System.currentTimeMillis()

    fun setFilter(value: Boolean) {
        player.getDataContainer()["filter"] = value
    }

    fun updateMuteTime(time: Long) {
        player.getDataContainer()["mute_time"] = System.currentTimeMillis() + time * 1000
    }

    internal fun addMessage(packet: Packet) {
        receivedMessages += ChatMessage(packet.source, packet.toMessage()?.replace("\\s".toRegex(), "")?.takeLast(32))
        if (receivedMessages.size > 100) {
            receivedMessages.removeFirstOrNull()
        }
    }

    fun removeMessage(message: String) {
        receivedMessages.removeIf { it.message == message }
    }

    fun releaseMessage() {
        val messages = ArrayList(receivedMessages)
        receivedMessages.clear()
        repeat(100) { player.sendMessage("") }
        messages.forEach { player.sendPacket(it.packet) }
    }

    companion object {

        @JvmField
        val SESSIONS = mutableMapOf<UUID, ChatSession>()

        fun getSession(player: Player): ChatSession {
            return SESSIONS.computeIfAbsent(player.uniqueId) { ChatSession(player, Settings.channelDefault.get(), onlinePlayers().map { it.cast<Player>() }.toSet()) }
        }

        fun removeSession(player: Player) {
            SESSIONS.remove(player.uniqueId)
        }

        private fun Packet.toMessage(): String? {
            return kotlin.runCatching {
                BaseComponent.toPlainText(*read<Array<BaseComponent>>("components")!!)
            }.getOrNull()
        }

        data class ChatMessage(val packet: Any, val message: String?)
    }
}