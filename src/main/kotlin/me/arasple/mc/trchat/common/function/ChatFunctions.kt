package me.arasple.mc.trchat.common.function

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.common.function.imp.Function
import me.arasple.mc.trchat.util.notify
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.configuration.ConfigSection
import taboolib.module.configuration.util.getMap
import kotlin.system.measureTimeMillis

/**
 * @author Arasple
 * @date 2019/11/30 14:19
 */
object ChatFunctions {

    val functions = mutableListOf<Function>()

    fun loadFunctions(vararg notify: ProxyCommandSender) {
        measureTimeMillis {
            functions.clear()

            TrChatFiles.function.getMap<String, ConfigSection>("CUSTOM").forEach { (name, funObj) ->
                functions.add(Function(name, funObj))
            }

            functions.sortBy { it.priority }
        }.also { notify(notify, "Plugin-Loaded-Functions", it) }
    }

    fun matchFunction(key: String): Function? {
        return functions.firstOrNull { f -> f.name.equals(key, ignoreCase = true) }
    }
}