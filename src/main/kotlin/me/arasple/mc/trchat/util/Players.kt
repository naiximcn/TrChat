package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.module.bungee.Bungees
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.*
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
            if (Bungees.isEnable) {
                submit(period = 60) {
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
        val players = mutableListOf<String>()
        players.addAll(Players.players)
        onlinePlayers().forEach { x ->
            if (!players.contains(x.name)) {
                players.add(x.name)
            }
        }
        return players
    }

    fun setPlayers(players: List<String>) {
        Players.players = players
    }
}