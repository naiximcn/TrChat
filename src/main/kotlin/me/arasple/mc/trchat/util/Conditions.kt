package me.arasple.mc.trchat.util

import org.apache.logging.log4j.util.Strings
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common5.Coerce
import taboolib.common5.compileJS
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.sendLang
import taboolib.platform.compat.replacePlaceholder
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.script.SimpleBindings

/**
 * @author Arasple, wlys
 * @date 2019/11/30 13:21
 */
fun checkCondition(player: Player, script: String?): Boolean {
    val condition = script?.replacePlaceholder(player)

    if (Strings.isEmpty(condition) || condition.equals("null", ignoreCase = true)) {
        return true
    }

    return when {
        condition!!.startsWith("\$:") -> checkJs(player, condition.substring(2))
        condition.startsWith("js:") -> checkJs(player, condition.substring(3))
        condition.startsWith("ke:") -> checkKether(player, condition.substring(3)).get()
        else -> checkKether(player, condition).get()
    }
}

private fun checkJs(player: Player, condition: String): Boolean {
    val bind = mapOf(
        "\$player" to player,
        "\$bukkitServer" to Bukkit.getServer()
    )
    try {
        return condition.compileJS()!!.eval(SimpleBindings(bind)) as Boolean
    } catch (e: Throwable) {
        adaptPlayer(player).sendLang("Error-Js", condition, e.message ?: "", Arrays.toString(e.stackTrace))
        console().sendLang("Error-Js", condition, e.message ?: "", Arrays.toString(e.stackTrace))
    }
    return false
}

private fun checkKether(player: Player, condition: String): CompletableFuture<Boolean> {
    return if (condition.isEmpty()) {
        CompletableFuture.completedFuture(true)
    } else {
        try {
            KetherShell.eval(condition) {
                sender = adaptPlayer(player)
            }.thenApply {
                Coerce.toBoolean(it)
            }
        } catch (e: LocalizedException) {
            e.printKetherErrorMessage()
            CompletableFuture.completedFuture(false)
        } catch (e: Throwable) {
            adaptPlayer(player).sendLang("Error-Kether", condition, e.message ?: "", Arrays.toString(e.stackTrace))
            console().sendLang("Error-Kether", condition, e.message ?: "", Arrays.toString(e.stackTrace))
            CompletableFuture.completedFuture(false)
        }
    }
}