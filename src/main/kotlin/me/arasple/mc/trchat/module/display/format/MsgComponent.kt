package me.arasple.mc.trchat.module.display.format

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.Function.Companion.replaceRegex
import me.arasple.mc.trchat.util.color.DefaultColor
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.color.colorify
import me.arasple.mc.trchat.util.hoverItemFixed
import me.arasple.mc.trchat.util.pass
import me.arasple.mc.trchat.util.proxy.bukkit.Players
import me.arasple.mc.trchat.util.proxy.sendProxyLang
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.io.digest
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.common.util.VariableReader
import taboolib.common.util.replaceWithOrder
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.uncolored
import taboolib.module.nms.getI18nName
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import taboolib.platform.util.serializeToByteArray
import java.util.*
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

    fun serialize(player: Player, msg: String, disabledFunctions: List<String>): TellrawJson {
        val tellraw = TellrawJson()

        var message = msg
        message = message.itemShow(player).mention().inventoryShow(player)

        Function.functions.filter { it.condition.pass(player) }.forEach {
            message = message.replaceRegex(it.regex, it.filterTextPattern, "{{${it.id}:{0}}}")
        }

        val defaultColor = MessageColors.catchDefaultMessageColor(player, defaultColor)

        parser.readToFlatten(message).forEach { part ->
            if (part.isVariable) {
                val args = part.text.split(":", limit = 2)
                when (val id = args[0]) {
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
                                    }, player).also {
                                        cache.put(item, it)
                                    }
                            }
                        })
                    }
                    "MENTION" -> {
                        tellraw.append(Functions.mention.getString("Format")!!.replaceWithOrder(player.name).colorify())
                        if (Functions.mention.getBoolean("Notify", true)) {
                            player.sendProxyLang(args[1], "Mentions-Notify", player.name)
                        }
                    }
                    "INVENTORY" -> {
                        val menu = buildMenu<Linked<ItemStack>>("${player.name} 的背包") {
                            rows(6)
                            slots(inventorySlots)
                            elements {
                                IntRange(9, 35).map { player.inventory.getItem(it) ?: AIR_ITEM } +
                                        IntRange(0, 8).map { player.inventory.getItem(it) ?: ItemStack(Material.AIR) }
                            }
                            onGenerate { _, element, _, _ ->
                                element
                            }
                            onBuild {
                                it.setItem(1, player.inventory.invokeMethod<ItemStack>("getItemInOffHand") ?: AIR_ITEM)
                                it.setItem(2, buildItem(XMaterial.PLAYER_HEAD) { name = "§e${player.name}" })
                                it.setItem(3, player.inventory.itemInHand)
                                it.setItem(5, player.inventory.helmet ?: AIR_ITEM)
                                it.setItem(6, player.inventory.chestplate ?: AIR_ITEM)
                                it.setItem(7, player.inventory.leggings ?: AIR_ITEM)
                                it.setItem(8, player.inventory.boots ?: AIR_ITEM)
                            }
                        }
                        val sha1 = Base64.getEncoder().encodeToString(player.inventory.serializeToByteArray()).digest("sha-1")
                        inventoryCache.put(sha1, menu)
                        tellraw
                            .append(Functions.inventoryShow.getString("Format")!!.replaceWithOrder(player.name).colorify())
                            .runCommand("/view-inventory $sha1")
                    }
                    else -> {
                        Function.functions.firstOrNull { it.id == id }?.let {
                            tellraw.append(it.displayJson.toTellrawJson(player, args[1]))
                            it.action?.let { action -> TrChatAPI.eval(player, action) }
                        }
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
        hover?.filter { it.condition.pass(player) }?.joinToString("\n") { it.process(tellraw, player, message = message) }?.let { tellraw.hoverText(it) }
        suggest?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message = message.uncolored())
        command?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message = message.uncolored())
        url?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message = message.uncolored())
        insertion?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message = message.uncolored())
        copy?.firstOrNull { it.condition.pass(player) }?.process(tellraw, player, message = message.uncolored())

        return tellraw
    }

    companion object {

        private val parser = VariableReader()

        val itemCache: Cache<ItemStack, TellrawJson> = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build()

        val inventoryCache: Cache<String, Inventory> = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .build()

        private val inventorySlots = IntRange(18, 53).toList()

        private val AIR_ITEM = buildItem(XMaterial.GRAY_STAINED_GLASS_PANE) { name = "Air" }

        fun ItemStack.getDisplayName(player: Player): String {
            if (isAir()) {
                return "空气"
            }
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
            if (Functions.mention.getBoolean("Enable")) {
                result = Players.regex.replace(result) {
                    "{{MENTION:${it.groupValues[1]}}}"
                }
            }
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