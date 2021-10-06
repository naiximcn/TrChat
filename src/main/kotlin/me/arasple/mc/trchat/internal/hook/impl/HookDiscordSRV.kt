package me.arasple.mc.trchat.internal.hook.impl

import github.scarsz.discordsrv.DiscordSRV
import me.arasple.mc.trchat.internal.hook.HookAbstract
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.function.submit

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
                DiscordSRV.getPlugin().processChatMessage(
                    e.player,
                    e.message,
                    DiscordSRV.getPlugin().getOptionalChannel("global"),
                    false
                )
            }
        }
    }
}