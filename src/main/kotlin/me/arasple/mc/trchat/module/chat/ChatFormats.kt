package me.arasple.mc.trchat.module.chat

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.module.chat.format.Format
import me.arasple.mc.trchat.module.chat.format.PriFormat
import me.arasple.mc.trchat.module.chat.obj.ChatType
import me.arasple.mc.trchat.util.checkCondition
import me.arasple.mc.trchat.util.notify
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender

/**
 * @author Arasple, wlys
 * @date 2019/11/30 12:09
 */
object ChatFormats {

    private val formats = HashMap<ChatType, List<Format>>()

    fun getFormat(type: ChatType, player: Player): Format? {
        return formats.computeIfAbsent(type) { ArrayList() }.sortedBy { it.priority }.firstOrNull { format ->
            checkCondition(player, format.requirement)
        }
    }

    fun loadFormats(vararg notify: ProxyCommandSender) {
        val start = System.currentTimeMillis()
        formats.entries.clear()

        for (chatType in ChatType.values()) {
            if (TrChatFiles.formats.contains(chatType.name)) {
                val formats = mutableListOf<Format>()
                TrChatFiles.formats.getMapList(chatType.name).forEach { formatMap ->
                    formats.add(if (chatType.isPrivate) PriFormat(formatMap) else Format(formatMap))
                }
                ChatFormats.formats[chatType] = formats
            }
        }

        formats[ChatType.PRIVATE_RECEIVE]!!.forEach { format -> format.msg.isPrivateChat = true }
        formats[ChatType.PRIVATE_SEND]!!.forEach { format -> format.msg.isPrivateChat = true }

        notify(notify, "Plugin-Loaded-Chat-Formats", System.currentTimeMillis() - start)
    }
}