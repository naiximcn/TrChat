package me.arasple.mc.trchat.common.channels

import com.google.common.collect.Lists
import me.arasple.mc.trchat.Metrics
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.proxy.Proxy
import org.bukkit.entity.Player
import taboolib.common.platform.function.onlinePlayers
import java.util.*

/**
 * @author Arasple
 * @date 2019/8/16 17:02
 */
object ChannelStaff {

    private val staffs: MutableList<UUID> = Lists.newArrayList()

    fun execute(player: Player, message: String) {
        if (player.hasPermission("trchat.staff")) {
            val format = ChatFormats.getFormat(ChatType.STAFF, player)!!.apply(player, message, post = false)
            if (Proxy.isEnabled) {
                val raw = format.toRawMessage()
                Proxy.sendProxyData(player, "TrChat", "SendRawPerm", raw, "trchat.staff")
            } else {
                onlinePlayers().filter { it.hasPermission("trchat.staff") }.forEach {
                    format.sendTo(it)
                }
            }
            Metrics.increase(0)
        }
    }

    fun switchStaff(player: Player): Boolean {
        if (!staffs.contains(player.uniqueId)) {
            staffs.add(player.uniqueId)
        } else {
            staffs.remove(player.uniqueId)
        }
        return staffs.contains(player.uniqueId)
    }

    fun isInStaffChannel(player: Player): Boolean {
        return staffs.contains(player.uniqueId)
    }
}