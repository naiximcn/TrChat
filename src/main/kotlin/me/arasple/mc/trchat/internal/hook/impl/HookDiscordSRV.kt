package me.arasple.mc.trchat.internal.hook.impl

import me.arasple.mc.trchat.internal.hook.HookAbstract
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * HookDynmap
 * me.arasple.mc.trchat.internal.hook.impl
 *
 * @author wlys
 * @since 2021/9/11 13:43
 */
class HookDiscordSRV : HookAbstract() {

    private val classPlayerChatListener: Class<*>? = try {
        Class.forName("github.scarsz.discordsrv.listeners.PlayerChatListener")
    } catch (e: ClassNotFoundException) {
        null
    }

    fun forwardChat(e: AsyncPlayerChatEvent) {
        if (isHooked) {
            kotlin.runCatching {
                classPlayerChatListener?.invokeMethod<Any>("onAsyncPlayerChat", e)
            }
        }
    }
}