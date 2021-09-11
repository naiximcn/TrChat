package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.common.filter.ChatFilter.filter
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import net.minecraft.server.v1_16_R3.IChatBaseComponent
import net.minecraft.server.v1_16_R3.NonNullList
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.Packet

/**
 * @author Arasple
 * @date 2019/11/30 11:16
 */
class PacketUtilsImpl : PacketUtils() {

    override fun filterIChatComponent(component: Any?): Any? {
        component ?: return component
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
        try {
            (items as List<*>).forEach { item -> filterItem(item) }
        } catch (t: Throwable) {
            try {
                (items as NonNullList<*>).forEach { item -> filterItem(item) }
            } catch (t2: Throwable) {
                kotlin.runCatching {
                    (items as Array<*>).forEach { item -> filterItem(item) }
                }
            }
        }
    }

    override fun packetToMessage(packet: Packet): String {
        return kotlin.runCatching {
            if (majorLegacy >= 11700) {
                if (packet.read<IChatBaseComponent>("message") != null) {
                    IChatBaseComponent.ChatSerializer.a(packet.read<IChatBaseComponent>("message"))
                } else {
                    ComponentSerializer.toString(*packet.read<Array<BaseComponent>>("components")!!)
                }
            } else {
                if (packet.read<IChatBaseComponent>("a") != null) {
                    IChatBaseComponent.ChatSerializer.a(packet.read<IChatBaseComponent>("a"))
                } else {
                    ComponentSerializer.toString(*packet.read<Array<BaseComponent>>("components")!!)
                }
            }
        }.getOrElse { "" }
    }
}