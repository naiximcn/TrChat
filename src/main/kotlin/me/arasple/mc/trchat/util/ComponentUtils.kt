package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common.reflect.Reflex.Companion.invokeMethod
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

fun legacy(string: String): TextComponent {
    return BukkitComponentSerializer.legacy().deserialize(string)
}

fun TextComponent.hoverItemFixed(item: ItemStack, player: Player): TextComponent {
    var newItem = item.optimizeShulkerBox()
    newItem = NMS.INSTANCE.optimizeNBT(newItem)
    newItem = HookPlugin.getEcoEnchants().displayItem(newItem, player)
    val nmsItemStack = TrChatAPI.classCraftItemStack.invokeMethod<Any>("asNMSCopy", newItem, fixed = true)!!
    val nmsNBTTabCompound = classNBTTagCompound.invokeConstructor()
    val itemJson = nmsItemStack.invokeMethod<Any>("save", nmsNBTTabCompound)!!
    val id = itemJson.invokeMethod<String>("getString", "id") ?: "minecraft:air"
    val tag = itemJson.invokeMethod<Any>("get", "tag")?.toString() ?: "{}"
    return hoverEvent(HoverEvent.showItem(Key.key(id), newItem.amount, BinaryTagHolder.of(tag)))
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