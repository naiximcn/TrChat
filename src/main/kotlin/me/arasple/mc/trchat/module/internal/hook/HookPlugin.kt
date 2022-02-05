package me.arasple.mc.trchat.module.internal.hook

import me.arasple.mc.trchat.module.internal.hook.impl.HookEcoEnchants
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

/**
 * @author Arasple
 * @date 2021/1/26 22:04
 */
@PlatformSide([Platform.BUKKIT])
object HookPlugin {

    fun printInfo() {
        registry.filter { it.isHooked }.forEach {
            console().sendLang("Plugin-Dependency-Hooked", it.name)
        }
    }

    private val registry: Array<HookAbstract> = arrayOf(
        HookEcoEnchants()
    )

    fun getEcoEnchants(): HookEcoEnchants {
        return registry[0] as HookEcoEnchants
    }

}