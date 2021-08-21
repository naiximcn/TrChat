package me.arasple.mc.trchat.internal.listener

import com.mojang.brigadier.suggestion.Suggestions
import me.arasple.mc.trchat.api.TrChatFiles
import org.bukkit.Bukkit
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.MinecraftVersion.majorLegacy
import taboolib.module.nms.PacketSendEvent

/**
 * @author Arasple
 * @date 2020/1/17 14:41
 */
@PlatformSide([Platform.BUKKIT])
object ListenerTabComplete {

    @SubscribeEvent
    fun onTabCommandSend(e: PacketSendEvent) {
        if (!MinecraftVersion.isSupported) return
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