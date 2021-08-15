package me.arasple.mc.trchat.api

import me.arasple.mc.trchat.module.data.DatabaseLocal
import me.arasple.mc.trchat.module.filter.ChatFilter.filter
import me.arasple.mc.trchat.module.filter.processer.Filter
import me.arasple.mc.trchat.module.filter.processer.FilteredObject
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta

/**
 * @author Arasple
 * @date 2019/8/18 0:18
 */
object TrChatAPI {

    val database by lazy {
        DatabaseLocal()
//        when (TrChatFiles.settings.getString("Database.method")!!.uppercase(Locale.getDefault())) {
//            "LOCAL" -> DatabaseLocal()
//            "MONGODB" -> DatabaseMongodb()
//            else -> error("Storage method \"${TrChatFiles.settings.getString("Database.method")}\" not supported.")
//        }
    }

    /**
     * 根据玩家的权限，过滤的字符串
     *
     * @param player 玩家
     * @param string 字符串
     * @return 过滤后的
     */
    fun filterString(player: Player, string: String): FilteredObject {
        return Filter.doFilter(string, !player.hasPermission("trchat.bypass.filter"))
    }

    fun filterString(player: Player, string: String, execute: Boolean): FilteredObject {
        return if (execute) filterString(player, string) else FilteredObject(string, 0)
    }

    fun filterItemStack(itemStack: ItemStack) {
        if (itemStack.isAir()) {
            return
        }
        itemStack.modifyMeta<ItemMeta> {
            if (hasDisplayName()) {
                setDisplayName(filter(displayName).filtered)
            }
            modifyLore {
                if (isNotEmpty()) {
                    for (i in indices) {
                        set(i, filter(get(i)).filtered)
                    }
                }
            }
        }
    }

    @Awake(LifeCycle.DISABLE)
    internal fun e() {
        onlinePlayers().forEach { database.push(it.cast()) }
    }

    @SubscribeEvent
    internal fun e(e: PlayerQuitEvent) {
        submit(async = true) {
            database.push(e.player)
            database.release(e.player)
        }
    }
}