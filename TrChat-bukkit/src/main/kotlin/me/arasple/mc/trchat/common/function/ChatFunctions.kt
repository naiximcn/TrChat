package me.arasple.mc.trchat.common.function

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.common.function.imp.Function
import me.arasple.mc.trchat.util.notify
import taboolib.common.platform.ProxyCommandSender
import taboolib.library.configuration.MemorySection

/**
 * @author Arasple
 * @date 2019/11/30 14:19
 */
object ChatFunctions {

    val functions = mutableListOf<Function>()

    fun loadFunctions(vararg notify: ProxyCommandSender) {
        val start = System.currentTimeMillis()
        functions.clear()

        TrChatFiles.function.getConfigurationSection("CUSTOM").getValues(false).forEach { (name, funObj) ->
            functions.add(Function(name, funObj as MemorySection))
        }

        functions.sortBy { it.priority }

        notify(notify, "Plugin-Loaded-Functions", System.currentTimeMillis() - start)
    }

    fun matchFunction(key: String): Function? {
        return functions.firstOrNull { f -> f.name.equals(key, ignoreCase = true) }
    }
}