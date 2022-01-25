package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.api.Functions
import me.arasple.mc.trchat.module.display.format.part.*
import me.arasple.mc.trchat.util.DefaultColor
import me.arasple.mc.trchat.util.Variables
import me.arasple.mc.trmenu.util.colorify
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.getI18nName
import taboolib.platform.util.buildItem
import java.util.HashMap
import java.util.function.Function

/**
 * @author wlys
 * @since 2021/12/12 13:46
 */
class MsgComponent(
    var defaultColor: DefaultColor?,
    hover: List<Hover>?,
    suggest: List<Suggest>?,
    command: List<Command>?,
    url: List<Url>?,
    insertion: List<Insertion>) : JsonComponent(null, null, hover, suggest, command, url, insertion) {

    fun itemShow() {
        functions.add { msg ->
            val tellraw = TellrawJson()
            if (!Functions.itemShow.getBoolean("Enable")) {
                return@add tellraw
            }
            var message = msg
            for (key in Functions.itemShow.getStringList("Keys")) {
                message = message.replace(key, "%i", ignoreCase = true)
            }
            Variables(message, "%i(-\\d)?".toRegex()) { it.getOrElse(1) { inventory.heldItemSlot.toString() }.removePrefix("-") }
                .element.map {
                    if (it.isVariable) {
                        val item = inventory.getItem(it.value.toInt()) ?: ItemStack(Material.AIR)
                        tellraw.append(itemCache.computeIfAbsent(item) {
                            TellrawJson()
                                .append(Functions.itemShow.getString("Format")!!.colorify().replaceWithOrder(item.getDisplayName(this), item.amount.toString()))
                                .hoverItemFixed(if (Functions.itemShow.getBoolean("Compatible")) {
                                    buildItem(item) { material = Material.STONE }
                                } else {
                                    item
                                })
                        })
                    } else {
                        tellraw.append(defaultColor?.colored())
                    }
                }
            tellraw
        }
}

    fun serialize(player: Player, msg: String): TellrawJson {
        val tellraw = TellrawJson()

        var message = msg
        message = message.itemShow().mention()

        return tellraw
    }

    companion object {

        val itemCache = HashMap<ItemStack, TellrawJson>()

        fun ItemStack.getDisplayName(player: Player): String {
            return if ((Functions.itemShow.getBoolean("Origin-Name", false)
                        || itemMeta == null) || !itemMeta!!.hasDisplayName()
            ) {
                getI18nName(player)
            } else {
                itemMeta!!.displayName
            }
        }

        private fun String.itemShow(): String {
            var result = this
            if (Functions.itemShow.getBoolean("Enable")) {
                for (key in Functions.itemShow.getStringList("Keys")) {
                    val regex = Regex("(?i)$key(-[1-9])?")
                    regex.findAll(result).forEach {

                    }
                    result = result.replace(.toRegex(), "{{ITEM:$-1}}")
                }
            }
            return result
        }

        private fun String.mention(): String {
            var result = this
            return result
        }
    }
}