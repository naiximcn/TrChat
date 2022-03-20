package me.arasple.mc.trchat.api.nms

import me.arasple.mc.trchat.api.config.Filter
import me.arasple.mc.trchat.module.display.filter.ChatFilter.filter
import me.arasple.mc.trchat.util.getSession
import net.kyori.adventure.platform.bukkit.MinecraftComponentSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.PacketSendEvent

/**
 * @author Arasple
 * @date 2019/11/30 10:16
 */
@PlatformSide([Platform.BUKKIT])
object NMSListener {

    @SubscribeEvent(EventPriority.LOWEST)
    fun e(e: PacketSendEvent) {
        // Chat Filter
        when (e.packet.name) {
            "PacketPlayOutChat" -> {
                e.player.getSession().addMessage(e.packet)
                if (!Filter.CONF.getBoolean("Filter.Chat") || !e.player.getSession().isFilterEnabled) {
                    return
                }
                val message = if (majorLegacy >= 11700) {
                    e.packet.read<Any>("message")!!
                } else {
                    e.packet.read<Any>("a")!!
                }
                if (MinecraftComponentSerializer.isSupported()){
                    val component = MinecraftComponentSerializer.get().deserialize(message)
                    if (majorLegacy >= 11700) {
                        e.packet.write("message", MinecraftComponentSerializer.get().serialize(filterComponent(component)))
                    } else {
                        e.packet.write("a", MinecraftComponentSerializer.get().serialize(filterComponent(component)))
                    }
                }
//                kotlin.runCatching {
//                    val components = e.packet.read<Array<BaseComponent>>("components") ?: return
//                    e.packet.write("components", components.map { filterComponent(it) }.toTypedArray())
//                }
                return
            }
            "PacketPlayOutWindowItems" -> {
                if (!Filter.CONF.getBoolean("Filter.Item") || !e.player.getSession().isFilterEnabled) {
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
                if (!Filter.CONF.getBoolean("Filter.Item") || !e.player.getSession().isFilterEnabled) {
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

    private fun filterComponent(component: Component): Component {
        return if (component is TextComponent && component.content().isNotEmpty()) {
            component.content(filter(component.content()).filtered)
        } else if (component.children().isNotEmpty()) {
            Component.text {
                component.children().forEach { it.append(filterComponent(it)) }
            }

        } else {
            component
        }
    }

//    private fun filterComponent(component: BaseComponent): BaseComponent {
//        if (component is TextComponent && component.text.isNotEmpty()) {
//            component.text = filter(component.text).filtered
//        }
//        if (!component.extra.isNullOrEmpty()) {
//            component.extra = component.extra.map { filterComponent(it) }
//        }
//        return component
//    }
}