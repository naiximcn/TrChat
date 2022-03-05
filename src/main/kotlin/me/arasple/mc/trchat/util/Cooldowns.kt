package me.arasple.mc.trchat.util

import org.bukkit.entity.Player
import java.util.*

/**
 * @author wlys
 * @since 2022/3/5 14:09
 */
class Cooldowns {

    var cooldowns = mutableListOf<Cooldown>()

    class Cooldown(var id: String, var time: Long)

    companion object {

        private val COOLDOWNS = mutableMapOf<UUID, Cooldowns>()

        fun getCooldownLeft(uuid: UUID, type: CooldownType): Long {
            return COOLDOWNS.putIfAbsent(uuid, Cooldowns())!!.cooldowns.firstOrNull {
                it.id == type.alias
            }?.let { it.time - System.currentTimeMillis() } ?: -1
        }

        fun isInCooldown(uuid: UUID, type: CooldownType): Boolean {
            return getCooldownLeft(uuid, type) > 0
        }

        fun updateCooldown(uuid: UUID, type: CooldownType, lasts: Long) {
            COOLDOWNS.putIfAbsent(uuid, Cooldowns())!!.let {
                it.cooldowns.removeIf { it.id == type.alias }
                it.cooldowns.add(Cooldown(type.alias, System.currentTimeMillis() + lasts))
            }
        }

    }
}

enum class CooldownType(val alias: String) {

    /**
     * Chat Cooldown Types
     */

    CHAT("Chat"),
    ITEM_SHOW("ItemShow"),
    MENTION("Mention"),
    INVENTORY_SHOW("InventoryShow")

}

fun Player.getCooldownLeft(type: CooldownType) = Cooldowns.getCooldownLeft(uniqueId, type)

fun Player.isInCooldown(type: CooldownType) = Cooldowns.isInCooldown(uniqueId, type)

fun Player.updateCooldown(type: CooldownType, lasts: Long) = Cooldowns.updateCooldown(uniqueId, type, lasts)