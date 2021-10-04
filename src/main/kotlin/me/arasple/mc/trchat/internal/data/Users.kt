package me.arasple.mc.trchat.internal.data

import me.arasple.mc.trchat.common.channel.impl.ChannelCustom
import me.arasple.mc.trchat.internal.data.Cooldowns.Cooldown
import me.arasple.mc.trchat.internal.data.Cooldowns.CooldownType
import me.arasple.mc.trchat.internal.data.Database.database
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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

    fun getCooldownLeft(uuid: UUID, type: CooldownType): Long {
        cooldowns.putIfAbsent(uuid, Cooldowns())
        for (COOLDOWN in cooldowns[uuid]!!.cooldowns) {
            if (COOLDOWN.id.equals(type.alias, ignoreCase = true)) {
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
        cooldowns[uuid]!!.cooldowns.removeIf { c -> c.id.equals(type.alias, ignoreCase = true) }
        cooldowns[uuid]!!.cooldowns.add(Cooldown(type.alias, System.currentTimeMillis() + lasts))
    }

    fun isFilterEnabled(user: Player): Boolean {
        return database.pull(user).getBoolean("FILTER", true)
    }

    fun setFilter(user: Player, value: Boolean) {
        database.pull(user).set("FILTER", value)
    }

    fun getLastMessage(uuid: UUID): String {
        return originMessage.getOrDefault(uuid, "")
    }

    fun setLastMessage(uuid: UUID, msg: String) {
        originMessage[uuid] = msg
    }

    fun updateMuteTime(user: Player, time: Long) {
        database.pull(user).set("MUTE_TIME", System.currentTimeMillis() + time * 1000)
    }

    fun isMuted(user: Player): Boolean {
       return database.pull(user).getLong("MUTE_TIME", 0) > System.currentTimeMillis()
    }

    fun setCustomChannel(user: Player, channel: ChannelCustom?) {
        database.pull(user).set("CUSTOM-CHANNEL", channel?.name)
    }

    fun getCustomChannel(user: Player): ChannelCustom? {
        return ChannelCustom.list.firstOrNull { it.name == database.pull(user).getString("CUSTOM-CHANNEL", "") }
    }

    fun removeCustomChannel(user: Player) {
        val channel = getCustomChannel(user) ?: return
        if (channel.hint){
            user.sendLang("Custom-Channel-Quit", channel.name)
        }
        setCustomChannel(user, null)
    }
}