package me.arasple.mc.trchat.internal.data

import me.arasple.mc.trchat.internal.database.DatabaseLocal
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit

/**
 * Database
 * me.arasple.mc.trchat.internal.data
 *
 * @author wlys
 * @since 2021/9/11 13:29
 */
@PlatformSide([Platform.BUKKIT])
object Database {

    val database by lazy { DatabaseLocal() }

    @Schedule(delay = 100, period = 20 * 30, async = true)
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