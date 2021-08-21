package me.arasple.mc.trchat.common.chat

import me.arasple.mc.trchat.api.nms.PacketUtils
import me.arasple.mc.trchat.internal.data.Users
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.module.nms.PacketSendEvent
import taboolib.module.nms.sendPacket
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * MessageTransmit
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/8/11 19:26
 */
@PlatformSide([Platform.BUKKIT])
object MessageTransmit {

    private val playerMessageCache = ConcurrentHashMap<UUID, MutableList<String>>()

    @SubscribeEvent(priority = EventPriority.MONITOR)
    fun e(e: PacketSendEvent) {
        if (e.packet.name == "PacketPlayOutChat") {
            putMessage(e.player, PacketUtils.INSTANCE.packetToMessage(e.packet))
        }
    }

    private fun putMessage(player: Player, message: String) {
        val messages = playerMessageCache.computeIfAbsent(player.uniqueId) { ArrayList() }
        messages += message
        if (messages.size > 100) {
            messages.removeAt(0)
        }
    }

    // TODO
    fun Player.releaseTransmit() {

    }
}