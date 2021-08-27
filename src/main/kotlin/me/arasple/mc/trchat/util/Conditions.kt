package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.internal.script.JavaScriptAgent
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common5.Coerce
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.sendLang
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.sendLang
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author Arasple, wlys
 * @date 2019/11/30 13:21
 */
fun checkCondition(player: Player, condition: String?): Boolean {
    val script = condition?.replacePlaceholder(player)

    if (script.isNullOrEmpty() || script.equals("null", ignoreCase = true)) {
        return true
    }

    val (isJavaScript, js) = JavaScriptAgent.serialize(script)

    return if (isJavaScript) JavaScriptAgent.eval(player, js!!) as Boolean
    else checkKether(player, script.removePrefix("ke:")).get()
}

private fun checkKether(player: Player, script: String): CompletableFuture<Boolean> {
    return try {
        KetherShell.eval(script) {
            sender = adaptPlayer(player)
        }.thenApply {
            Coerce.toBoolean(it)
        }
    } catch (e: LocalizedException) {
        e.printKetherErrorMessage()
        CompletableFuture.completedFuture(false)
    } catch (e: Throwable) {
        player.sendLang("Error-Kether", script, e.message.toString(), Arrays.toString(e.stackTrace))
        console().sendLang("Error-Kether", script, e.message.toString(), Arrays.toString(e.stackTrace))
        CompletableFuture.completedFuture(false)
    }
}