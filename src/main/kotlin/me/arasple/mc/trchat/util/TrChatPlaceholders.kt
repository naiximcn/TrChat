package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.internal.data.Users.isFilterEnabled
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.platform.compat.PlaceholderExpansion

/**
 * TrChatPlaceholders
 * me.arasple.mc.trchat.internal.hook
 *
 * @author Arasple
 * @since 2021/8/9 23:09
 */
@PlatformSide([Platform.BUKKIT])
object TrChatPlaceholders : PlaceholderExpansion {

    override val identifier: String
        get() = "trchat"

    override fun onPlaceholderRequest(player: Player, args: String): String {
        if (!player.isOnline) {
            return ""
        }

        return when {
            args.equals("filter", ignoreCase = true) -> isFilterEnabled(player).toString()
            else -> ""
        }
    }
}