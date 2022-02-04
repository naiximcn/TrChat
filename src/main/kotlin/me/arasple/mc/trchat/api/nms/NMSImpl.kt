package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.common.filter.ChatFilter.filter
import net.minecraft.server.v1_16_R3.NBTBase
import net.minecraft.server.v1_16_R3.NBTTagCompound
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion.isUniversal
import taboolib.platform.util.isNotAir

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
        } catch (e: Throwable) {
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
}