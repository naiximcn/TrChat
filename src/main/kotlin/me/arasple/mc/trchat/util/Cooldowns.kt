package me.arasple.mc.trchat.util

import org.bukkit.entity.Player
import java.util.*

/**
 * @author wlys
 * @since 2022/3/5 14:09
 */
class Cooldowns {

    var cooldowns = mutableMapOf<String, Long>()

    companion object {

        private val COOLDOWNS = mutableMapOf<UUID, Cooldowns>()

        fun getCooldownLeft(uuid: UUID, type: CooldownType): Long {
            return COOLDOWNS.computeIfAbsent(uuid) { Cooldowns() }
                .cooldowns[type.alias]?.let { it - System.currentTimeMillis() } ?: -1
        }

        fun isInCooldown(uuid: UUID, type: CooldownType): Boolean {
            return getCooldownLeft(uuid, type) > 0
        }

        fun updateCooldown(uuid: UUID, type: CooldownType, lasts: Long) {
            COOLDOWNS.computeIfAbsent(uuid) { Cooldowns() }.let { cooldowns ->
                cooldowns.cooldowns[type.alias] = System.currentTimeMillis() + lasts
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