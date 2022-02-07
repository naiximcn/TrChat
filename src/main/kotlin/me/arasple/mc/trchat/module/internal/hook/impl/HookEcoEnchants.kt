package me.arasple.mc.trchat.module.internal.hook.impl

import com.willfp.eco.core.display.Display
import me.arasple.mc.trchat.module.internal.hook.HookAbstract
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author wlys
 * @since 2022/2/5 22:30
 */
class HookEcoEnchants : HookAbstract() {

    fun displayItem(item: ItemStack, player: Player?): ItemStack {
        if (!isHooked) {
            return item
        }
        return Display.displayAndFinalize(item, player)
    }
}