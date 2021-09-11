package me.arasple.mc.trchat.internal.hook.impl

import me.arasple.mc.trchat.internal.hook.HookAbstract
import org.bukkit.entity.Player
import org.dynmap.DynmapAPI
import taboolib.module.chat.TellrawJson

/**
 * HookDynmap
 * me.arasple.mc.trchat.internal.hook.impl
 *
 * @author wlys
 * @since 2021/9/11 13:43
 */
class HookDynmap : HookAbstract() {

    fun forwardChat(player: Player, msg: TellrawJson) {
        if (isHooked) {
            (plugin as DynmapAPI).postPlayerMessageToWeb(player, msg.toLegacyText())
        }
    }
}