package me.arasple.mc.trchat.internal.hook

import me.arasple.mc.trchat.internal.hook.impl.HookDiscordSRV
import me.arasple.mc.trchat.internal.hook.impl.HookDynmap
import me.arasple.mc.trchat.internal.hook.impl.HookItemsAdder
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
        HookDynmap(),
        HookDiscordSRV(),
        HookItemsAdder()
    )

    fun getDynmap(): HookDynmap {
        return registry[0] as HookDynmap
    }

    fun getDiscordSRV(): HookDiscordSRV {
        return registry[1] as HookDiscordSRV
    }

    fun getItemsAdder(): HookItemsAdder {
        return registry[2] as HookItemsAdder
    }

}