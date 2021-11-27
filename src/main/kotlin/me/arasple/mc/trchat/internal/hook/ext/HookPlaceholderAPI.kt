package me.arasple.mc.trchat.internal.hook.ext

import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.script.js.JavaScriptAgent
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
object HookPlaceholderAPI : PlaceholderExpansion {

    override val identifier: String
        get() = "trchat"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        if (player != null && player.isOnline) {
            val params = args.split("_")

            return when(params[0].lowercase()) {
                "filter" -> Users.isFilterEnabled(player).toString()
                "channel" -> Users.getCustomChannel(player)?.name ?: "null"
                "js" -> if (params.size > 1) JavaScriptAgent.eval(player, params[1]).asString() else ""
                else -> ""
            }.toString()
        }

        return "__"
    }
}