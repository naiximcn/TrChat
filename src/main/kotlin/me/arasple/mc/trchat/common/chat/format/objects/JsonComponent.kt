package me.arasple.mc.trchat.common.chat.format.objects

import me.arasple.mc.trchat.api.TrChatAPI
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
import taboolib.common.util.replaceWithOrder
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
        this.hover = convertHoverText(hover)
        this.suggest = suggest
        this.command = command
        this.url = url
        this.copy = copy
    }

    constructor(partSection: LinkedHashMap<*, *>) {
        if (partSection.containsKey("text")) {
            text = partSection["text"].toString()
        }
        if (partSection.containsKey("hover")) {
            hover = convertHoverText(partSection["hover"])
        }
        if (partSection.containsKey("suggest")) {
            suggest = partSection["suggest"].toString()
        }
        if (partSection.containsKey("command")) {
            command = partSection["command"].toString()
        }
        if (partSection.containsKey("url")) {
            url = partSection["url"].toString()
        }
        if (partSection.containsKey("requirement")) {
            requirement = partSection["requirement"].toString()
        }
        if (partSection.containsKey("copy")) {
            copy = partSection["copy"].toString()
        }
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
        tellraw.append(text?.replaceWithOrder(*vars)?.replacePlaceholder(player)?.coloredAll() ?: "§8[§fNull§8]")
        hover?.let { tellraw.hoverText(it.replaceWithOrder(*vars).replacePlaceholder(player).coloredAll()) }
        suggest?.let { tellraw.suggestCommand(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        command?.let { tellraw.runCommand(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        url?.let { tellraw.openURL(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        copy?.let { tellraw.copyOrSuggest(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        return tellraw
    }

    private fun convertHoverText(any: Any?): String {
        return if (any is List<*>) {
            any.joinToString("\n") { it.toString() }
        } else {
            any.toString()
        }
    }

    companion object {

        private val classNBTTagCompound by lazy {
            nmsClass("NBTTagCompound")
        }

        fun loadList(parts: Any): List<JsonComponent> {
            return (parts as LinkedHashMap<*, *>).values.map { part -> JsonComponent(part as LinkedHashMap<*, *>) }
        }

        fun TellrawJson.copyOrSuggest(text: String) {
            try {
                copyToClipboard(text)
            } catch (e: NoSuchFieldError) {
                suggestCommand(text)
            }
        }

        fun TellrawJson.hoverItemFixed(item: ItemStack): TellrawJson {
            val nmsItemStack = TrChatAPI.classCraftItemStack.invokeMethod<Any>("asNMSCopy", item, fixed = true)!!
            val nmsNBTTabCompound = classNBTTagCompound.invokeConstructor()
            val itemJson = nmsItemStack.invokeMethod<Any>("save", nmsNBTTabCompound)!!
            val id = itemJson.invokeMethod<String>("getString", "id") ?: "air"
            val tag = itemJson.invokeMethod<Any>("get", "tag")?.toString() ?: "{}"
            componentsLatest.forEach {
                try {
                    it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, Item(id, item.amount, ItemTag.ofNbt(tag)))
                } catch (ex: NoClassDefFoundError) {
                    it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder(itemJson.toString()).create())
                }
            }
            return this
        }
    }
}