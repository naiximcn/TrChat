package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.module.filter.ChatFilter.filter
import net.minecraft.server.v1_16_R3.IChatBaseComponent
import net.minecraft.server.v1_16_R3.NonNullList
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
class PacketUtilsImpl : PacketUtils() {

    override fun filterIChatComponent(component: Any?): Any? {
        component ?: return component
        return try {
            val raw = IChatBaseComponent.ChatSerializer.a(component as IChatBaseComponent)
            val filtered = filter(raw).filtered
            IChatBaseComponent.ChatSerializer.a(filtered)!!
        } catch (e: Throwable) {
            component
        }
    }

    override fun filterItem(item: Any?) {
        item ?: return
        try {
            val itemStack = CraftItemStack.asCraftMirror(item as net.minecraft.server.v1_16_R3.ItemStack)
            TrChatAPI.filterItemStack(itemStack)
        } catch (ignored: Exception) {
        }
    }

    override fun filterItemList(items: Any?) {
        items ?: return
        try {
            (items as List<*>).forEach { item -> filterItem(item) }
        } catch (e: Throwable) {
            try {
                (items as NonNullList<*>).forEach { item -> filterItem(item) }
            } catch (e2: Throwable) {
                (items as Array<*>).forEach { item -> filterItem(item) }
            }
        }
    }
}