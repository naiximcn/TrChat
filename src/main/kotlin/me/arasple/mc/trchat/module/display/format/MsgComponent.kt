package me.arasple.mc.trchat.module.display.format

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.util.color.DefaultColor
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.hoverItemFixed
import me.arasple.mc.trchat.util.pass
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.util.VariableReader
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.uncolored
import taboolib.module.nms.getI18nName
import taboolib.module.ui.Menu
import taboolib.platform.util.buildItem
import java.util.concurrent.TimeUnit

/**
 * @author wlys
 * @since 2021/12/12 13:46
 */
class MsgComponent(
    var defaultColor: DefaultColor,
    hover: List<Hover>?,
    suggest: List<Suggest>?,
    command: List<Command>?,
    url: List<Url>?,
    insertion: List<Insertion>?,
    copy: List<Copy>?
) : JsonComponent(null, hover, suggest, command, url, insertion, copy) {

    fun serialize(player: Player, msg: String): TellrawJson {
        val tellraw = TellrawJson()

        var message = msg
        message = message.itemShow(player).mention().inventoryShow(player)

        val defaultColor = MessageColors.catchDefaultMessageColor(player, defaultColor)

        parser.readToFlatten(message).forEach { part ->
            if (part.isVariable) {
                val args = part.text.split(":", limit = 2)
                when (args[0]) {
                    "ITEM" -> {
                        tellraw.append(itemCache.let { cache ->
                            val item = player.inventory.getItem(args[1].toInt()) ?: ItemStack(Material.AIR)
                            cache.getIfPresent(item) ?: kotlin.run {
                                TellrawJson()
                                    .append(Functions.itemShow.getString("Format")!!.replaceWithOrder(item.getDisplayName(player), item.amount.toString()).colorify())
                                    .hoverItemFixed(item.run {
                                        if (Functions.itemShow.getBoolean("Compatible", false)) {
                                            buildItem(this) { material = Material.STONE }
                                        } else {
                                            this
                                        }
                                    }).also {
                                        cache.put(item, it)
                                    }
                            }
                        })
                    }
                    "MENTION" -> {

                    }
                    "INVENTORY" -> {

                    }
                }
            } else {
                tellraw.append(toTellrawJson(player, MessageColors.defaultColored(defaultColor, player, message)))
            }
        }

        return tellraw
    }

    override fun toTellrawJson(player: Player, vararg vars: String): TellrawJson {
        val tellraw = TellrawJson()
        val message = vars[0]

        tellraw.append(message)
        hover?.filter { it.condition.pass(player) }?.joinToString("\n") { it.process(tellraw, player, message) }?.let { tellraw.hoverText(it) }
        suggest?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message.uncolored())
        command?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message.uncolored())
        url?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message.uncolored())
        insertion?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message.uncolored())
        copy?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message.uncolored())

        return tellraw
    }

    companion object {

        private val parser = VariableReader()

        val itemCache: Cache<ItemStack, TellrawJson> = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build()

        val inventoryCache: Cache<Long, Menu> = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build()

        fun ItemStack.getDisplayName(player: Player): String {
            return if (Functions.itemShow.getBoolean("Origin-Name", false)
                        || itemMeta?.hasDisplayName() != true
            ) {
                getI18nName(player)
            } else {
                itemMeta!!.displayName
            }
        }

        private fun String.itemShow(player: Player): String {
            var result = this
            if (Functions.itemShow.getBoolean("Enable")) {
                Functions.itemShowKeys.get().forEach { regex ->
                    result = result.replace(regex) {
                        "{{ITEM:${it.groups[1]?.value?.takeLast(1)?.toInt() ?: player.inventory.heldItemSlot}}}"
                    }
                }
            }
            return result
        }

        private fun String.mention(): String {
            var result = this
            return result
        }

        private fun String.inventoryShow(player: Player): String {
            var result = this
            if (Functions.inventoryShow.getBoolean("Enable")) {
                Functions.inventoryShow.getStringList("Keys").forEach {
                    result = result.replace(it, "{{INVENTORY:SELF}}", ignoreCase = true)
                }
            }
            return result
        }
    }
}