package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.api.config.Settings
import me.arasple.mc.trchat.module.display.channel.Channel
import me.arasple.mc.trchat.util.checkMute
import me.arasple.mc.trchat.util.getSession
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.Strings
import taboolib.platform.util.sendLang

/**
 * @author Arasple, wlys
 * @date 2019/11/30 12:10
 */
@PlatformSide([Platform.BUKKIT])
object ListenerChatEvent {

    private val hooks = arrayOf(
        "Dynmap",
        "DiscordSRV"
    )

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        e.isCancelled = true
        val player = e.player
        val message = e.message
        val session = player.getSession()

        session.recipients = e.recipients

        if (!player.checkMute()) {
            return
        }

        if (!checkLimits(player, message)) {
            return
        }

        Channel.channels
            .firstOrNull { it.bindings.prefix?.any { prefix -> message.startsWith(prefix, ignoreCase = true) } == true }
            ?.execute(player, message)
            ?: kotlin.run { session.channel?.execute(player, message) }

        e.handlers.registeredListeners
            .filter { hooks.contains(it.plugin.name)
                    && it.plugin.isEnabled
                    && it.priority == org.bukkit.event.EventPriority.MONITOR }.forEach {
            try {
                it.callEvent(AsyncPlayerChatEvent(e.isAsynchronous, e.player, message, e.recipients))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun checkLimits(p: Player, message: String): Boolean {
        if (p.hasPermission("trchat.bypass.*")) {
            return true
        }
        if (!p.hasPermission("trchat.bypass.chatlength")) {
            if (message.length > Settings.chatLengthLimit) {
                p.sendLang("General-Too-Long", message.length, Settings.chatLengthLimit)
                return false
            }
        }
        if (!p.hasPermission("trchat.bypass.repeat")) {
            val lastMessage = p.getSession().lastMessage
            if (Strings.similarDegree(lastMessage, message) > Settings.chatSimilarity) {
                p.sendLang("General-Too-Similar")
                return false
            } else {
                p.getSession().lastMessage = message
            }
        }
        if (!p.hasPermission("trchat.bypass.chatcd")) {
            if (!Settings.chatDelay.get().hasNext(p.name)) {
                p.sendLang("Cooldowns-Chat", Settings.CONF.getDouble("Chat.Cooldown").toString())
                return false
            }
        }
        if (!p.hasPermission("trchat.bypass.itemcd")) {
            if (Functions.itemShowKeys.get().any { it.matches(message) } && !Functions.itemShowDelay.get().hasNext(p.name)) {
                p.sendLang("Cooldowns-Item-Show", Functions.itemShow.getDouble("Cooldowns").toString())
                return false
            }
        }
        if (!p.hasPermission("trchat.bypass.inventorycd")) {
            if (Functions.inventoryShow.getStringList("Keys").any { message.contains(it, ignoreCase = true) }
                && !Functions.itemShowDelay.get().hasNext(p.name)) {
                p.sendLang("Cooldowns-Item-Show", Functions.itemShow.getDouble("Cooldowns").toString())
                return false
            }
        }
        return true
    }
}