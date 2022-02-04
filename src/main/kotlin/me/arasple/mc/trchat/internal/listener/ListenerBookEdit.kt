package me.arasple.mc.trchat.internal.listener

import me.arasple.mc.trchat.api.TrChatFiles.settings
import me.arasple.mc.trchat.util.color.MessageColors
import org.bukkit.event.player.PlayerEditBookEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

/**
 * @author Arasple, wlys
 * @date 2019/8/15 21:18
 */
@PlatformSide([Platform.BUKKIT])
object ListenerBookEdit {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBookEdit(e: PlayerEditBookEvent) {
        val p = e.player
        if (settings.getBoolean("CHAT-COLOR.BOOK", true)) {
            val meta = e.newBookMeta
            meta.pages = MessageColors.replaceWithPermission(p, meta.pages)
            e.newBookMeta = meta
        }
    }
}