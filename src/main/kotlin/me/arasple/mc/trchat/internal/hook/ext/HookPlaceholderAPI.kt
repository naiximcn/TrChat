package me.arasple.mc.trchat.internal.hook.ext

import me.arasple.mc.trchat.module.script.js.JavaScriptAgent
import me.arasple.mc.trchat.util.getSession
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
            val session = player.getSession()

            return when(params[0].lowercase()) {
                "filter" -> session.isFilterEnabled
                "channel" -> session.channel?.id
                "js" -> if (params.size > 1) JavaScriptAgent.eval(player, params[1]).get() else ""
                else -> ""
            }.toString()
        }

        return "__"
    }
}