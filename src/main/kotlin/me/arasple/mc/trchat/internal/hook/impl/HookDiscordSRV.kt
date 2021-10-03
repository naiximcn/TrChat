package me.arasple.mc.trchat.internal.hook.impl

import me.arasple.mc.trchat.internal.hook.HookAbstract
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.function.submit
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * HookDynmap
 * me.arasple.mc.trchat.internal.hook.impl
 *
 * @author wlys
 * @since 2021/9/11 13:43
 */
class HookDiscordSRV : HookAbstract() {

    fun forwardChat(e: AsyncPlayerChatEvent) {
        if (isHooked) {
            submit(async = true) {
                plugin!!.invokeMethod<Any>(
                    "processChatMessage",
                    e.player,
                    e.message,
                    plugin!!.invokeMethod<Any>("getOptionalChannel", "global"),
                    e.isCancelled
                )
            }
        }
    }
}