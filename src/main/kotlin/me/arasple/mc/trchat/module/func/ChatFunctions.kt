package me.arasple.mc.trchat.module.func

import me.arasple.mc.trchat.api.TrChatFiles.function
import me.arasple.mc.trchat.module.func.imp.Function
import me.arasple.mc.trchat.util.notify
import taboolib.common.platform.ProxyCommandSender
import taboolib.library.configuration.MemorySection

/**
 * @author Arasple
 * @date 2019/11/30 14:19
 */
object ChatFunctions {

    val functions: MutableList<Function> = ArrayList()

    fun loadFunctions(vararg notify: ProxyCommandSender) {
        val start = System.currentTimeMillis()
        functions.clear()

        function.getConfigurationSection("CUSTOM").getValues(false).forEach { (name, funObj) ->
            functions.add(Function(name, (funObj as MemorySection)))
        }

        notify(notify, "Plugin-Loaded-Functions", System.currentTimeMillis() - start)
    }

    fun mathFunction(key: String): Function? {
        return functions.firstOrNull { f -> f.name.equals(key, ignoreCase = true) }
    }
}