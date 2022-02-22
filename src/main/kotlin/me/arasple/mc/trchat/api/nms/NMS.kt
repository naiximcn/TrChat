package me.arasple.mc.trchat.api.nms

import org.bukkit.inventory.ItemStack
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.nmsProxy

/**
 * @author Arasple
 * @date 2019/11/30 11:17
 */
abstract class NMS {

    abstract fun filterItem(item: Any?)

    abstract fun filterItemList(items: Any?)

    abstract fun optimizeNBT(itemStack: ItemStack, nbtWhitelist: Array<String> = TellrawJson.whitelistTags): ItemStack

    companion object {

        val INSTANCE by lazy {
            nmsProxy<NMS>()
        }
    }
}
