package me.arasple.mc.trchat.internal.script

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.internal.script.js.JavaScriptAgent
import org.bukkit.entity.Player

/**
 * Condition
 * me.arasple.mc.trchat.internal.script.js
 *
 * @author wlys
 * @since 2021/8/29 15:16
 */
inline class Condition(private val script: String?) {

    fun eval(player: Player): EvalResult {
        return if (script.isNullOrEmpty() || script.equals("null", ignoreCase = true)) EvalResult.TRUE
        else eval(player, script)
    }

    companion object {

        fun eval(player: Player, script: String?): EvalResult {
            if (script.isNullOrEmpty() || script.equals("null", ignoreCase = true)) {
                return EvalResult.TRUE
            }
            val (isJavaScript, js) = JavaScriptAgent.serialize(script)
            return if (isJavaScript) JavaScriptAgent.eval(player, js!!)
            else TrChatAPI.instantKether(player, script.removePrefix("ke:"))
        }
    }
}