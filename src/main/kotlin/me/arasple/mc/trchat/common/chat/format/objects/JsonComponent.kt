package me.arasple.mc.trchat.common.chat.format.objects

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.internal.script.Condition
import me.arasple.mc.trchat.util.coloredAll
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.ItemTag
import net.md_5.bungee.api.chat.hover.content.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.common.util.asList
import taboolib.common.util.replaceWithOrder
import taboolib.common.util.sync
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.nmsClass
import taboolib.platform.compat.replacePlaceholder

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent {

    var requirement: String? = null
    var text: String? = null
    var hover: String? = null
    var suggest: String? = null
    var command: String? = null
    var url: String? = null
    var copy: String? = null

    constructor(text: String?, hover: List<String?>?, suggest: String?, command: String?, url: String?, copy: String?) {
        this.text = text
        this.hover = hover?.joinToString("\n")
        this.suggest = suggest
        this.command = command
        this.url = url
        this.copy = copy
    }

    constructor(partSection: LinkedHashMap<*, *>) {
        text = partSection["text"]?.toString()
        hover = partSection["hover"]?.asList()?.joinToString("\n")
        suggest = partSection["suggest"]?.toString()
        command = partSection["command"]?.toString()
        url = partSection["url"]?.toString()
        requirement = partSection["requirement"]?.toString()
        copy = partSection["copy"]?.toString()
    }

    fun toTellrawJson(player: Player, vararg vars: String, function: Boolean = false): TellrawJson {
        val tellraw = TellrawJson()
        if (!Condition.eval(player, requirement).asBoolean()) {
            return tellraw
        }

        var text = text

        if (vars.size == 1 && !function) {
            text = vars[0]
        }
        if (vars.isNotEmpty()) {
            if (vars[0].toBoolean()) {
                text = text!!.replace("%toplayer_name%", vars[1])
            }
        }
        tellraw.append(text?.replaceWithOrder(*vars)?.replacePlaceholderFixed(player)?.coloredAll() ?: "§8[§fNull§8]")
        hover?.let { tellraw.hoverText(it.replaceWithOrder(*vars).replacePlaceholderFixed(player).coloredAll()) }
        suggest?.let { tellraw.suggestCommand(it.replaceWithOrder(*vars).replacePlaceholderFixed(player)) }
        command?.let { tellraw.runCommand(it.replaceWithOrder(*vars).replacePlaceholderFixed(player)) }
        url?.let { tellraw.openURL(it.replaceWithOrder(*vars).replacePlaceholderFixed(player)) }
        copy?.let { tellraw.copyOrSuggest(it.replaceWithOrder(*vars).replacePlaceholderFixed(player)) }
        return tellraw
    }

    companion object {

        private val classNBTTagCompound by lazy {
            nmsClass("NBTTagCompound")
        }

        fun loadList(parts: Any): List<JsonComponent> {
            return (parts as List<*>).map { part -> JsonComponent((part as Map<*, *>).toMap(LinkedHashMap())) }
        }

        fun TellrawJson.copyOrSuggest(text: String) {
            try {
                copyToClipboard(text)
            } catch (_: NoSuchFieldError) {
                suggestCommand(text)
            }
        }

        fun TellrawJson.hoverItemFixed(item: ItemStack): TellrawJson {
            val newItem = NMS.INSTANCE.optimizeNBT(NMS.INSTANCE.optimizeShulkerBox(item))
            val nmsItemStack = TrChatAPI.classCraftItemStack.invokeMethod<Any>("asNMSCopy", newItem, fixed = true)!!
            val nmsNBTTabCompound = classNBTTagCompound.invokeConstructor()
            val itemJson = nmsItemStack.invokeMethod<Any>("save", nmsNBTTabCompound)!!
            val id = itemJson.invokeMethod<String>("getString", "id") ?: "air"
            val tag = itemJson.invokeMethod<Any>("get", "tag")?.toString() ?: "{}"
            componentsLatest.forEach {
                try {
                    it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, Item(id, item.amount, ItemTag.ofNbt(tag)))
                } catch (_: NoClassDefFoundError) {
                    it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder(itemJson.toString()).create())
                }
            }
            return this
        }

        fun String.replacePlaceholderFixed(player: Player): String {
            return try {
                replacePlaceholder(player)
            } catch (_: Throwable) {
                kotlin.runCatching { sync { replacePlaceholder(player) } }.getOrNull() ?: this
            }
        }
    }
}