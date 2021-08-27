package me.arasple.mc.trchat.internal.listener

import com.mojang.brigadier.suggestion.Suggestions
import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.api.nms.PacketUtils
import me.arasple.mc.trchat.common.filter.ChatFilter.filter
import me.arasple.mc.trchat.internal.data.Users.isFilterEnabled
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
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
        if (isFilterEnabled(e.player)) {
            when (e.packet.name) {
                "PacketPlayOutChat" -> {
                    if (majorLegacy >= 11700) {
                        e.packet.write("message", PacketUtils.INSTANCE.filterIChatComponent(e.packet.read<Any>("message")))
                    } else {
                        e.packet.write("a", PacketUtils.INSTANCE.filterIChatComponent(e.packet.read<Any>("a")))
                    }
                    kotlin.runCatching {
                        val components = e.packet.read<Array<BaseComponent>>("components") ?: return
                        val raw = ComponentSerializer.toString(*components)
                        val filtered = filter(raw).filtered
                        e.packet.write("components", ComponentSerializer.parse(filtered))
                    }
                    return
                }
                "PacketPlayOutWindowItems" -> {
                    if (majorLegacy >= 11700) {
                        PacketUtils.INSTANCE.filterItemList(e.packet.read<Any>("items"))
                    } else {
                        PacketUtils.INSTANCE.filterItemList(e.packet.read<Any>("b"))
                    }
                    return
                }
                "PacketPlayOutSetSlot" -> {
                    if (majorLegacy >= 11700) {
                        PacketUtils.INSTANCE.filterItem(e.packet.read<Any>("itemStack"))
                    } else {
                        PacketUtils.INSTANCE.filterItem(e.packet.read<Any>("c"))
                    }
                    return
                }
            }
        }
        // Tab Complete
        if (TrChatFiles.settings.getBoolean("GENERAL.PREVENT-TAB-COMPLETE", false)
            && e.packet.name == "PacketPlayOutTabComplete"
            && !e.player.hasPermission("trchat.bypass.tabcomplete")) {
            if (majorLegacy >= 11700) {
                e.isCancelled = (e.packet.read<Suggestions>("suggestions") ?: Suggestions.empty().get())
                    .list.none { Bukkit.getPlayerExact(it.text) != null }
            } else if (majorLegacy >= 11300) {
                e.isCancelled = (e.packet.read<Suggestions>("b") ?: Suggestions.empty().get())
                    .list.none { Bukkit.getPlayerExact(it.text) != null }
            } else {
                e.isCancelled = listOf(*e.packet.read<Array<String>>("a") ?: emptyArray())
                    .none { Bukkit.getPlayerExact(it) != null }
            }
        }
    }
}