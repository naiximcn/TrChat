package me.arasple.mc.trchat.api

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.dynmap.DynmapAPI
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.console
import taboolib.module.chat.TellrawJson
import taboolib.module.lang.sendLang

/**
 * TrChatHook
 * me.arasple.mc.trchat.api
 *
 * @author wlys
 * @since 2021/8/19 13:10
 */
@PlatformSide([Platform.BUKKIT])
object TrChatHook {

    var isDynmapHooked = false
        private set
    private var dynmap: Any? = null

    @Awake(LifeCycle.ENABLE)
    fun init() {
        isDynmapHooked = Bukkit.getServer().pluginManager.isPluginEnabled("dynmap")
        if (isDynmapHooked) {
            dynmap = Bukkit.getServer().pluginManager.getPlugin("dynmap")
            console().sendLang("Plugin-Dependency-Hooked", "Dynmap")
        }
    }

    fun postToDynmap(player: Player, msg: TellrawJson) {
        if (isDynmapHooked) {
            (dynmap as DynmapAPI).postPlayerMessageToWeb(player, msg.toLegacyText())
        }
    }
}