package me.arasple.mc.trchat.module.channels

import com.google.common.collect.Lists
import me.arasple.mc.trchat.Metrics
import me.arasple.mc.trchat.module.chat.ChatFormats
import me.arasple.mc.trchat.module.chat.obj.ChatType
import me.arasple.mc.trchat.module.bungee.Bungees
import net.md_5.bungee.chat.ComponentSerializer
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
            if (Bungees.isEnable) {
                val raw = ComponentSerializer.toString(*format.componentsAll.toTypedArray())
                Bungees.sendBungeeData(player, "TrChat", "SendRawPerm", raw, "trchat.staff")
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