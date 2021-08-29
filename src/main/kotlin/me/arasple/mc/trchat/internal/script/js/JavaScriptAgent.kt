package me.arasple.mc.trchat.internal.script.js

import com.google.common.collect.Maps
import me.arasple.mc.trchat.internal.script.EvalResult
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.console
import taboolib.common5.compileJS
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.util.*
import javax.script.CompiledScript
import javax.script.ScriptContext
import javax.script.SimpleBindings
import javax.script.SimpleScriptContext

/**
 * JavaScriptAgent
 * me.arasple.mc.trchat.internal.script.js
 *
 * @author wlys
 * @since 2021/8/27 16:10
 */
object JavaScriptAgent {

    private val prefixes = arrayOf(
        "js:",
        "$ ",
        "$:"
    )

    private val bindings = mapOf(
        "bukkitServer" to Bukkit.getServer(),
        "utils" to Assist.INSTANCE
    )

    private val compiledScripts = Maps.newConcurrentMap<String, CompiledScript>()

    fun preCompile(script: String): CompiledScript {
        return compiledScripts.computeIfAbsent(script) {
            script.compileJS()
        }
    }

    fun serialize(script: String): Pair<Boolean, String?> {
        prefixes.firstOrNull { script.startsWith(it) }?.let {
            return true to script.removePrefix(it)
        }
        return false to null
    }

    fun eval(player: Player, script: String): EvalResult {
        return try {
            val context = SimpleScriptContext()

            context.setBindings(SimpleBindings(bindings).also {
                it["player"] = player
            }, ScriptContext.ENGINE_SCOPE)

            EvalResult(preCompile(script).eval(context))
        } catch (e: Throwable) {
            println("§c[TrChat] §8Unexpected exception while parsing javascript:")
            e.localizedMessage.split("\n").forEach {
                println("         §8$it")
            }
            EvalResult.FALSE
        }
    }
}