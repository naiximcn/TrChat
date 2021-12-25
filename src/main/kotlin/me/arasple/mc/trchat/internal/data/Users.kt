package me.arasple.mc.trchat.internal.data

import me.arasple.mc.trchat.common.channel.impl.ChannelCustom
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.expansion.getDataContainer
import taboolib.module.chat.TellrawJson
import taboolib.platform.util.sendLang
import java.util.*

/**
 * @author Arasple
 * @date 2019/11/30 11:30
 */
object Users {


    val formattedMessages = HashMap<UUID, MutableList<String>>()

    fun setCustomChannel(user: Player, channel: ChannelCustom?) {
        user.getDataContainer()["custom_channel"] = channel?.name ?: ""
    }

    fun getCustomChannel(user: Player): ChannelCustom? {
        val current = user.getDataContainer()["custom_channel"]?.ifEmpty { return null } ?: return null
        return ChannelCustom.list.firstOrNull { it.name == current }
    }

    fun removeCustomChannel(user: Player) {
        val channel = getCustomChannel(user) ?: return
        if (channel.hint){
            user.sendLang("Custom-Channel-Quit", channel.name)
        }
        setCustomChannel(user, null)
    }
}