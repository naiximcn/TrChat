package me.arasple.mc.trchat.internal.listener

import me.arasple.mc.trchat.api.nms.PacketUtils
import me.arasple.mc.trchat.internal.data.Users.isFilterEnabled
import me.arasple.mc.trchat.common.filter.ChatFilter.filter
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.PacketSendEvent

/**
 * @author Arasple
 * @date 2019/11/30 10:16
 */
@PlatformSide([Platform.BUKKIT])
object FilterListener {

    @SubscribeEvent
    fun filterPacket(e: PacketSendEvent) {
        if (!MinecraftVersion.isSupported) return
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
                }
                "PacketPlayOutWindowItems" -> {
                    if (majorLegacy >= 11700) {
                        PacketUtils.INSTANCE.filterItemList(e.packet.read<Any>("items"))
                    } else {
                        PacketUtils.INSTANCE.filterItemList(e.packet.read<Any>("b"))
                    }
                }
                "PacketPlayOutSetSlot" -> {
                    if (majorLegacy >= 11700) {
                        PacketUtils.INSTANCE.filterItem(e.packet.read<Any>("itemStack"))
                    } else {
                        PacketUtils.INSTANCE.filterItem(e.packet.read<Any>("c"))
                    }
                }
            }
        }
    }
}