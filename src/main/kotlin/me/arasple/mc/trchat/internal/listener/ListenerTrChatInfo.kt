package me.arasple.mc.trchat.internal.listener

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.filter.ChatFilter
import me.arasple.mc.trchat.common.function.ChatFunctions
import org.apache.logging.log4j.util.Strings
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.platform.*
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.pluginVersion
import taboolib.common.util.replaceWithOrder
import taboolib.library.xseries.XSound

/**
 * @author Arasple
 * @date 2019/11/30 21:56
 */
@PlatformSide([Platform.BUKKIT])
object ListenerTrChatInfo {

    @SubscribeEvent(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        e.isCancelled = react(e.player, if (e.message.startsWith("#")) e.message.substring(1) else null)

        if (e.message == "#TRCHAT-RELOAD" && e.player.hasPermission("trchat.admin")) {
            TrChatFiles.formats.reload()
            ChatFormats.loadFormats(adaptPlayer(e.player))
            TrChatFiles.filter.reload()
            ChatFilter.loadFilter(true, adaptPlayer(e.player))
            TrChatFiles.function.reload()
            ChatFunctions.loadFunctions(adaptPlayer(e.player))
            e.isCancelled = true
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        e.isCancelled = react(e.player, e.message.substring(1))
    }

    private fun react(p: Player, message: String?): Boolean {
        if (!Strings.isBlank(message) && ("trchat".equals(message, ignoreCase = true) || "trixeychat".equals(message, ignoreCase = true))) {
            adaptPlayer(p).sendTitle("§3§lTr§b§lChat", "§7Designed by §6Arasple", 10, 35, 10)
            adaptPlayer(p).sendActionBar("§2Running version §av{0}§7".replaceWithOrder(pluginVersion))
            XSound.BLOCK_NOTE_BLOCK_PLING.play(p, 1f, 2f)
            return true
        }
        return false
    }
}