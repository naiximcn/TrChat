package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.module.display.format.part.*
import me.arasple.mc.trchat.module.script.Condition
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.ItemTag
import net.md_5.bungee.api.chat.hover.content.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.nmsClass

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent(
    val condition: Condition?,
    val text: List<Text>?,
    val hover: List<Hover>?,
    val suggest: List<Suggest>?,
    val command: List<Command>?,
    val url: List<Url>?,
    val insertion: List<Insertion>?
) {

    open fun toTellrawJson(player: Player, vararg vars: String): TellrawJson {
        val tellraw = TellrawJson()
        if (condition?.eval(player) == false) {
            return tellraw
        }

        text!!.firstOrNull { it.condition?.eval(player) ?: true }?.process(tellraw, player)
        hover?.firstOrNull { it.condition?.eval(player) ?: true }?.process(tellraw, player)
        suggest?.firstOrNull { it.condition?.eval(player) ?: true }?.process(tellraw, player)
        command?.firstOrNull { it.condition?.eval(player) ?: true }?.process(tellraw, player)
        url?.firstOrNull { it.condition?.eval(player) ?: true }?.process(tellraw, player)
        insertion?.firstOrNull { it.condition?.eval(player) ?: true }?.process(tellraw, player)

        return tellraw
    }

    companion object {

        private val classNBTTagCompound by lazy {
            nmsClass("NBTTagCompound")
        }

        internal fun TellrawJson.hoverItemFixed(item: ItemStack): TellrawJson {
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