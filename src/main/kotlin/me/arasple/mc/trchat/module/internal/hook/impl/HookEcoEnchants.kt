package me.arasple.mc.trchat.module.internal.hook.impl

import me.arasple.mc.trchat.module.internal.hook.HookAbstract
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * @author wlys
 * @since 2022/2/5 22:30
 */
class HookEcoEnchants : HookAbstract() {

    fun displayItem(item: ItemStack, player: Player?) {
        if (!isHooked) {
            return
        }
        plugin!!.invokeMethod<Any>("getDisplayModule")!!.invokeMethod<Any>("display", item, player, false)
    }
}