package me.arasple.mc.trchat.module.display.function

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.hoverItemFixed
import me.arasple.mc.trchat.util.legacy
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.util.replaceWithOrder
import taboolib.common5.util.parseMillis
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.nms.getI18nName
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import java.util.concurrent.TimeUnit

/**
 * @author wlys
 * @since 2022/3/12 19:14
 */
@PlatformSide([Platform.BUKKIT])
object ItemShow {

    @ConfigNode("General.Item-Show.Enabled", "function.yml")
    var enabled = true

    @ConfigNode("General.Item-Show.Format", "function.yml")
    var format = "&8[&3{0} &bx{1}&8]"

    @ConfigNode("General.Item-Show.Compatible", "function.yml")
    var compatible = false

    @ConfigNode("General.Item-Show.Origin-Name", "function.yml")
    var originName = false

    @ConfigNode("General.Item-Show.Cooldown", "function.yml")
    val cooldown = ConfigNodeTransfer<String, Long> { parseMillis() }

    @ConfigNode("General.Item-Show.Keys", "function.yml")
    val keys = ConfigNodeTransfer<List<String>, List<Regex>> { map { Regex("$it(-[1-9])?") } }

    private val cache: Cache<ItemStack, Component> = CacheBuilder.newBuilder()
        .expireAfterWrite(10L, TimeUnit.MINUTES)
        .build()

    fun replaceMessage(message: String, player: Player): String {
        return if (!enabled) {
            message
        } else {
            var result = message
            keys.get().firstOrNull { it.containsMatchIn(result) }?.let { regex ->
                result = result.replace(regex) {
                    "{{ITEM:${it.groups[1]?.value?.takeLast(1)?.toInt() ?: player.inventory.heldItemSlot}}}"
                }
            }
            return result
        }
    }

    fun createComponent(player: Player, slot: Int): Component {
        val item = player.inventory.getItem(slot) ?: ItemStack(Material.AIR)
        return cache.getIfPresent(item) ?: kotlin.run {
            legacy(format.replaceWithOrder(item.getDisplayName(player), item.amount.toString()).colorify())
                .hoverItemFixed(item.run {
                    if (compatible) {
                        buildItem(this) { material = Material.STONE }
                    } else {
                        this.clone()
                    }
                }, player).also {
                    cache.put(item, it)
                }
        }
    }

    private fun ItemStack.getDisplayName(player: Player): String {
        if (isAir()) {
            return "空气"
        }
        return if (originName || itemMeta?.hasDisplayName() != true) {
            getI18nName(player)
        } else {
            itemMeta!!.displayName
        }
    }
}