package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.common.filter.ChatFilter.filter
import net.minecraft.server.v1_16_R3.NBTBase
import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.module.nms.getI18nName
import taboolib.platform.util.isNotAir
import taboolib.platform.util.modifyMeta

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
class NMSImpl : NMS() {

    override fun filterIChatComponent(component: Any?): Any? {
        component ?: return null
        return try {
            val raw = TrChatAPI.classChatSerializer.invokeMethod<String>("a", component, fixed = true)!!
            val filtered = filter(raw).filtered
            TrChatAPI.classChatSerializer.invokeMethod<Any>("a", filtered, fixed = true)
        } catch (_: Throwable) {
            component
        }
    }

    override fun filterItem(item: Any?) {
        item ?: return
        kotlin.runCatching {
            val itemStack = TrChatAPI.classCraftItemStack.invokeMethod<ItemStack>("asCraftMirror", item, fixed = true)!!
            TrChatAPI.filterItemStack(itemStack)
        }
    }

    override fun filterItemList(items: Any?) {
        items ?: return
        kotlin.runCatching {
            (items as List<*>).forEach { item -> filterItem(item) }
        }.onFailure {
            kotlin.runCatching {
                (items as Array<*>).forEach { item -> filterItem(item) }
            }
        }
    }

    override fun optimizeNBT(itemStack: ItemStack, nbtWhitelist: Array<String>): ItemStack {
        try {
            val nmsItem = TrChatAPI.classCraftItemStack
                .invokeMethod<net.minecraft.server.v1_16_R3.ItemStack>("asNMSCopy", itemStack, fixed = true)!!
            if (itemStack.isNotAir() && nmsItem.hasTag()) {
                val nbtTag = NBTTagCompound()
                val mapNew = nbtTag.getProperty<HashMap<String, NBTBase>>(if (isUniversal) "tags" else "map")!!
                val mapOrigin = nmsItem.tag?.getProperty<Map<String, NBTBase>>(if (isUniversal) "tags" else "map") ?: return itemStack
                mapOrigin.entries.forEach {
                    if (nbtWhitelist.contains(it.key)) {
                        mapNew[it.key] = it.value
                    }
                }
                nmsItem.tag = nbtTag
                return TrChatAPI.classCraftItemStack.invokeMethod<ItemStack>("asBukkitCopy", nmsItem, fixed = true)!!
            }
        } catch (_: Throwable) {
        }
        return itemStack
    }

    override fun optimizeShulkerBox(item: ItemStack): ItemStack {
        try {
            if (!item.type.name.endsWith("SHULKER_BOX")) {
                return item
            }
            val itemClone = item.clone()
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
        return item
    }
}