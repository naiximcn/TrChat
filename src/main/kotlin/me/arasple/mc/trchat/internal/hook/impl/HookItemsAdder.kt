package me.arasple.mc.trchat.internal.hook.impl

import dev.lone.itemsadder.api.FontImages.FontImageWrapper
import me.arasple.mc.trchat.internal.hook.HookAbstract
import org.bukkit.entity.Player

/**
 * HookDynmap
 * me.arasple.mc.trchat.internal.hook.impl
 *
 * @author wlys
 * @since 2021/9/11 13:43
 */
class HookItemsAdder : HookAbstract() {

    fun replaceFontImages(player: Player, message: String): String {
        return if (isHooked) {
            FontImageWrapper.replaceFontImages(player, message)
        } else {
            message
        }
    }
}