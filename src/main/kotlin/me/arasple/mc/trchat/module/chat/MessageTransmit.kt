package me.arasple.mc.trchat.module.chat

import me.arasple.mc.trchat.module.data.Users
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
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

    private val playerMessageCache = ConcurrentHashMap<UUID, MutableList<Any>>()

    @SubscribeEvent
    fun e(e: PacketSendEvent) {
        if (e.packet.name == "PacketPlayOutChat") {
            val message = playerMessageCache.computeIfAbsent(e.player.uniqueId) { ArrayList() }
            message += e.packet.source
        }
    }

    // TODO
    fun Player.releaseTransmit() {
        Users.formatedMessage[uniqueId] = ""
        playerMessageCache.entries.forEach { (k, v) ->
            v.forEach {
                if (it.getProperty<Any>(if (isUniversal) "message" else "a") == Users.formatedMessage[uniqueId]) {
                    playerMessageCache[k]!!.remove(it)
                }
            }
        }
        onlinePlayers().forEach { player ->
            playerMessageCache[player.uniqueId]?.forEachIndexed { index, packet ->
                player.cast<Player>().sendPacket(packet)
            }
        }
    }
}