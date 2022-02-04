package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.nms.NMS
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.ItemTag
import net.md_5.bungee.api.chat.hover.content.Item
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.getI18nName
import taboolib.module.nms.nmsClass
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyMeta

/**
 * @author wlys
 * @since 2022/2/4 12:54
 */

private val classNBTTagCompound by lazy {
    nmsClass("NBTTagCompound")
}

fun TellrawJson.hoverItemFixed(item: ItemStack): TellrawJson {
    val newItem = NMS.INSTANCE.optimizeNBT(item.optimizeShulkerBox())
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

private fun ItemStack.optimizeShulkerBox(): ItemStack {
    try {
        if (!type.name.endsWith("SHULKER_BOX")) {
            return this
        }
        val itemClone = clone()
        val blockStateMeta = itemClone.itemMeta!! as BlockStateMeta
        val shulkerBox = blockStateMeta.blockState as ShulkerBox
        val contents = shulkerBox.inventory.contents
        val contentsClone = contents.mapNotNull {
            if (it.isNotAir()) {
                ItemStack(Material.STONE, it.amount, it.durability).modifyMeta<ItemMeta> {
                    if (it.itemMeta?.hasDisplayName() == true) {
                        setDisplayName(it.itemMeta!!.displayName)
                    } else {
                        setDisplayName(it.getI18nName())
                    }
                }
            } else {
                null
            }
        }.toTypedArray()
        shulkerBox.inventory.contents = contentsClone
        blockStateMeta.blockState = shulkerBox
        itemClone.itemMeta = blockStateMeta
        return itemClone
    } catch (_: Throwable) {
    }
    return this
}