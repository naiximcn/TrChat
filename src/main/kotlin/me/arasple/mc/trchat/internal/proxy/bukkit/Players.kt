package me.arasple.mc.trchat.internal.proxy.bukkit

import me.arasple.mc.trchat.internal.proxy.Proxy
import me.arasple.mc.trchat.internal.proxy.bungee.Bungees
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.platform.function.submit

/**
 * @author Arasple
 * @date 2019/8/4 21:28
 */
@PlatformSide([Platform.BUKKIT])
object Players {

    private var players = listOf<String>()

    @Awake(LifeCycle.ENABLE)
    fun startTask() {
        submit(delay = 20) {
            if (Proxy.platform == Platform.BUNGEE && Proxy.isEnabled) {
                submit(period = 60, async = true) {
                    if (Bukkit.getOnlinePlayers().isNotEmpty()) {
                        Bungees.sendBungeeData(Bukkit.getOnlinePlayers().iterator().next(), "PlayerList", "ALL")
                    }
                }
            }
        }
    }

    /*
    GETTERS & SETTERS
     */

    fun isPlayerOnline(target: String): Boolean {
        val player = Bukkit.getPlayerExact(target)
        return player != null && player.isOnline || players.any { p -> p.equals(target, ignoreCase = true) }
    }

    fun getPlayerFullName(target: String): String? {
        val player = Bukkit.getPlayerExact(target)
        return if (player != null && player.isOnline) player.name else players.firstOrNull { p -> p.equals(target, ignoreCase = true) }
    }

    fun getPlayers(): List<String> {
        val players = mutableSetOf<String>()
        players += Players.players
        players += onlinePlayers().map { it.name }
        return players.filter { it.isNotBlank() }
    }

    fun setPlayers(players: List<String>) {
        Players.players = players
    }
}