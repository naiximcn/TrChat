package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.TrChatFiles.function
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.lang.sendLang

/**
 * @author Arasple, wlys
 * @date 2020/1/16 21:41
 */
@PlatformSide([Platform.BUKKIT])
object ListenerCommand {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        val command = e.message.removePrefix("/")
        val mCmd = Bukkit.getCommandAliases().entries.firstOrNull { (_, value) ->
            value.any { it.equals(command.split(" ")[0], ignoreCase = true) }
        }

        if (function.getBoolean("GENERAL.COMMAND-CONTROLLER.ENABLE", true) && command.isNotEmpty()
            && !player.hasPermission(function.getString("GENERAL.COMMAND-CONTROLLER.BYPASS", "trchat.admin")!!)) {
            val whitelist = function.getString("GENERAL.COMMAND-CONTROLLER.TYPE", "BLACKLIST").equals("WHITELIST", ignoreCase = true)
            val matches = function.getStringList("GENERAL.COMMAND-CONTROLLER.LIST")
            val matched = matches.any { m ->
                val exact = m.lowercase().contains("<exact>")
                val m2 = m.replace("(?i)<exact>".toRegex(), "")

                if (exact) {
                    return@any command.matches("(?i)$m2".toRegex())
                } else {
                    return@any command.split(" ").toTypedArray()[0].matches("(?i)$m2".toRegex()) || mCmd != null && mCmd.key.matches("(?i)$m2".toRegex())
                }
            }
            // 黑名单下，匹配到 或 白名单下，未匹配到
            if (matched && !whitelist || !matched && whitelist) {
                e.isCancelled = true
                adaptPlayer(player).sendLang("Command-Controller-Deny")
            }
        }
    }
}