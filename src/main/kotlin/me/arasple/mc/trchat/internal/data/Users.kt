package me.arasple.mc.trchat.internal.data

import me.arasple.mc.trchat.common.channel.impl.ChannelCustom
import me.arasple.mc.trchat.internal.data.Cooldowns.Cooldown
import me.arasple.mc.trchat.internal.data.Cooldowns.CooldownType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.expansion.getDataContainer
import taboolib.module.chat.TellrawJson
import taboolib.platform.util.sendLang
import java.util.*

/**
 * @author Arasple, wlys
 * @date 2019/11/30 11:30
 */
object Users {

    val itemCache = HashMap<ItemStack, TellrawJson>()
    private val cooldowns = HashMap<UUID, Cooldowns>()
    private val originMessage = HashMap<UUID, String>()
    val formattedMessages = HashMap<UUID, MutableList<String>>()

    fun getCooldownLeft(uuid: UUID, type: CooldownType): Long {
        cooldowns.putIfAbsent(uuid, Cooldowns())
        for (COOLDOWN in cooldowns[uuid]!!.cooldowns) {
            if (COOLDOWN.id == type.alias) {
                return COOLDOWN.time - System.currentTimeMillis()
            }
        }
        return -1
    }

    fun isInCooldown(uuid: UUID, type: CooldownType): Boolean {
        return getCooldownLeft(uuid, type) > 0
    }

    fun updateCooldown(uuid: UUID, type: CooldownType, lasts: Long) {
        cooldowns.putIfAbsent(uuid, Cooldowns())
        cooldowns[uuid]!!.cooldowns.removeIf { it.id == type.alias }
        cooldowns[uuid]!!.cooldowns.add(Cooldown(type.alias, System.currentTimeMillis() + lasts))
    }

    fun isFilterEnabled(user: Player): Boolean {
        return user.getDataContainer()["filter"].toBoolean()
    }

    fun setFilter(user: Player, value: Boolean) {
        user.getDataContainer()["filter"] = value
    }

    fun getLastMessage(uuid: UUID): String {
        return originMessage.getOrDefault(uuid, "")
    }

    fun setLastMessage(uuid: UUID, msg: String) {
        originMessage[uuid] = msg
    }

    fun putFormattedMessage(player: Player, message: String) {
        val messages = formattedMessages.computeIfAbsent(player.uniqueId) { ArrayList() }
        messages += message
        if (messages.size > 100) {
            messages.removeFirstOrNull()
        }
    }

    fun updateMuteTime(user: Player, time: Long) {
        user.getDataContainer()["mute_time"] = System.currentTimeMillis() + time * 1000
    }

    fun isMuted(user: Player): Boolean {
        return (user.getDataContainer()["mute_time"]?.toIntOrNull() ?: 0) > System.currentTimeMillis()
    }

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