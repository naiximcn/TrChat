package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.module.script.Condition
import me.arasple.mc.trchat.util.coloredAll
import me.arasple.mc.trmenu.util.colorify
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
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.nmsClass
import taboolib.platform.compat.replacePlaceholder

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent(
    val condition: Condition?,
    val text: String,
    val hover: String?,
    val suggest: String?,
    val command: String?,
    val url: String?,
    val copy: String?
) {

    open fun toTellrawJson(player: Player, vararg vars: String): TellrawJson {
        val tellraw = TellrawJson()
        if (condition?.eval(player) == false) {
            return tellraw
        }

        tellraw.append(text.replaceWithOrder(*vars).replacePlaceholder(player).colorify())
        hover?.let { tellraw.hoverText(it.replaceWithOrder(*vars).replacePlaceholder(player).colorify()) }
        suggest?.let { tellraw.suggestCommand(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        command?.let { tellraw.runCommand(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        url?.let { tellraw.openURL(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        copy?.let { tellraw.copyOrSuggest(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        return tellraw
    }

    companion object {

        private val classNBTTagCompound by lazy {
            nmsClass("NBTTagCompound")
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