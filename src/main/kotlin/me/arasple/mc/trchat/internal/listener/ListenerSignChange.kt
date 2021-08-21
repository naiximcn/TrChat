package me.arasple.mc.trchat.internal.listener

import me.arasple.mc.trchat.api.TrChatFiles.settings
import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.util.MessageColors
import org.bukkit.event.block.SignChangeEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

/**
 * @author Arasple, wlys
 * @date 2019/8/15 21:18
 */
@PlatformSide([Platform.BUKKIT])
object ListenerSignChange {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onSignChange(e: SignChangeEvent) {
        val p = e.player

        for (i in e.lines.indices) {
            var line = e.getLine(i)
            if (settings.getBoolean("CHAT-COLOR.SIGN")) {
                line = MessageColors.replaceWithPermission(p, line ?: "")
            }
            e.setLine(i, if (line != null) TrChatAPI.filterString(p, line, true).filtered else null)
        }
    }
}