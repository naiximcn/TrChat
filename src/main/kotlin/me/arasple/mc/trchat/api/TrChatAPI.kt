package me.arasple.mc.trchat.api

import me.arasple.mc.trchat.internal.database.DatabaseLocal
import me.arasple.mc.trchat.common.filter.ChatFilter.filter
import me.arasple.mc.trchat.common.filter.processer.Filter
import me.arasple.mc.trchat.common.filter.processer.FilteredObject
import me.arasple.mc.trchat.internal.script.EvalResult
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit
import taboolib.common5.mirrorNow
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.nms.nmsClass
import taboolib.module.nms.obcClass
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.modifyMeta
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * @author Arasple, wlys
 * @date 2019/8/18 0:18
 */
object TrChatAPI {

    /**
     * 根据玩家的权限，过滤的字符串
     *
     * @param player 玩家
     * @param string 字符串
     * @param execute 是否真的过滤
     * @return 过滤后的
     */
    @JvmStatic
    fun filterString(player: Player, string: String, execute: Boolean = true): FilteredObject {
        return if (execute) Filter.doFilter(string, !player.hasPermission("trchat.bypass.filter")) else FilteredObject(string, 0)
    }

    @JvmStatic
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

    @JvmStatic
    fun eval(player: Player, script: String): CompletableFuture<Any?> {
        return mirrorNow("API:Script:Evaluation") {
            return@mirrorNow try {
                KetherShell.eval(script, namespace = listOf("trmenu", "trhologram", "trchat")) {
                    sender = adaptPlayer(player)
                }
            } catch (e: LocalizedException) {
                println("§c[TrChat] §8Unexpected exception while parsing kether shell:")
                e.localizedMessage.split("\n").forEach {
                    println("         §8$it")
                }
                CompletableFuture.completedFuture(false)
            }
        }
    }

    @JvmStatic
    fun instantKether(player: Player, script: String, timeout: Long = 100): EvalResult {
        return try {
            EvalResult(eval(player, script).get(timeout, TimeUnit.MILLISECONDS))
        } catch (e: TimeoutException) {
            println("§c[TrChat] §8Timeout while parsing kether shell:")
            e.localizedMessage?.split("\n")?.forEach { println("         §8$it") }
            EvalResult.FALSE
        }
    }

    val classCraftItemStack by lazy {
        obcClass("inventory.CraftItemStack")
    }

    val classChatSerializer by lazy {
        nmsClass("IChatBaseComponent\$ChatSerializer")
    }
}