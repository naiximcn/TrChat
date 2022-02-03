package me.arasple.mc.trchat.internal.listener

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.api.nms.NMS
import me.arasple.mc.trchat.common.filter.ChatFilter.filter
import me.arasple.mc.trchat.internal.data.Users
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.PacketSendEvent

/**
 * @author Arasple
 * @date 2019/11/30 10:16
 */
@PlatformSide([Platform.BUKKIT])
object ListenerPackets {

    @SubscribeEvent
    fun e(e: PacketSendEvent) {
        // Chat Filter
        if (Users.isFilterEnabled(e.player)) {
            when (e.packet.name) {
                "PacketPlayOutChat" -> {
                    if (!TrChatFiles.filter.getBoolean("FILTER.CHAT")) {
                        return
                    }
//                    if (majorLegacy >= 11700) {
//                        e.packet.write("message", PacketUtils.INSTANCE.filterIChatComponent(e.packet.read<Any>("message")))
//                    } else {
//                        e.packet.write("a", PacketUtils.INSTANCE.filterIChatComponent(e.packet.read<Any>("a")))
//                    }
                    kotlin.runCatching {
                        val components = e.packet.read<Array<BaseComponent>>("components") ?: return
                        e.packet.write("components", components.map { filterComponent(it) }.toTypedArray())
                    }
                    return
                }
                "PacketPlayOutWindowItems" -> {
                    if (!TrChatFiles.filter.getBoolean("FILTER.ITEM")) {
                        return
                    }
                    if (majorLegacy >= 11700) {
                        NMS.INSTANCE.filterItemList(e.packet.read<Any>("items"))
                    } else {
                        NMS.INSTANCE.filterItemList(e.packet.read<Any>("b"))
                    }
                    return
                }
                "PacketPlayOutSetSlot" -> {
                    if (!TrChatFiles.filter.getBoolean("FILTER.ITEM")) {
                        return
                    }
                    if (majorLegacy >= 11700) {
                        NMS.INSTANCE.filterItem(e.packet.read<Any>("itemStack"))
                    } else {
                        NMS.INSTANCE.filterItem(e.packet.read<Any>("c"))
                    }
                    return
                }
            }
        }
        // Tab Complete
//        if (TrChatFiles.settings.getBoolean("GENERAL.PREVENT-TAB-COMPLETE", false)
//            && e.packet.name == "PacketPlayOutTabComplete"
//            && !e.player.hasPermission("trchat.bypass.tabcomplete")) {
//            if (majorLegacy >= 11700) {
//                e.isCancelled = (e.packet.read<Suggestions>("suggestions") ?: Suggestions.empty().get())
//                    .list.none { Bukkit.getPlayerExact(it.text) != null }
//            } else if (majorLegacy >= 11300) {
//                e.isCancelled = (e.packet.read<Suggestions>("b") ?: Suggestions.empty().get())
//                    .list.none { Bukkit.getPlayerExact(it.text) != null }
//            } else {
//                e.isCancelled = listOf(*e.packet.read<Array<String>>("a") ?: emptyArray())
//                    .none { Bukkit.getPlayerExact(it) != null }
//            }
//        }
    }

    private fun filterComponent(component: BaseComponent): BaseComponent {
        if (component is TextComponent && component.text.isNotEmpty()) {
            component.text = filter(component.text).filtered
        }
        if (!component.extra.isNullOrEmpty()) {
            component.extra = component.extra.map { filterComponent(it) }
        }
        return component
    }
}