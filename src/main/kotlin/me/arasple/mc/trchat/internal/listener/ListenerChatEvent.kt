package me.arasple.mc.trchat.internal.listener

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.common.channel.impl.ChannelGlobal
import me.arasple.mc.trchat.common.channel.impl.ChannelNormal
import me.arasple.mc.trchat.internal.data.Cooldowns
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.hook.HookPlugin
import me.arasple.mc.trchat.util.checkMute
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.Strings
import taboolib.module.lang.sendLang

/**
 * @author Arasple, wlys
 * @date 2019/11/30 12:10
 */
@PlatformSide([Platform.BUKKIT])
object ListenerChatEvent {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        val player = e.player
        // Disable worlds
        if (TrChatFiles.settings.getStringList("GENERAL.DISABLED-WORLDS").contains(player.world.name)) {
            e.isCancelled = true
            return
        }
        if (!player.checkMute()) {
            e.isCancelled = true
            return
        }
        // Limit
        if (!checkLimits(adaptPlayer(player), e.message)) {
            e.isCancelled = true
            return
        }
        // Custom Channel
        val channel = Users.getCustomChannel(player)
        if (channel != null) {
            e.isCancelled = true
            TrChatEvent(channel, player, e.message).call()
            return
        }
        // DiscordSRV
        HookPlugin.getDiscordSRV().forwardChat(e)
        // Global
        val globalPrefix = TrChatFiles.channels.getString("FORCE-GLOBAL-PREFIX", "!all")
        if (TrChatFiles.channels.getBoolean("FORCE-GLOBAL", false)
            || e.message.startsWith(globalPrefix)) {
            e.isCancelled = true
            TrChatEvent(ChannelGlobal, player, e.message.removePrefix(globalPrefix)).call()
            return
        }
        // Normal
        e.isCancelled = true
        ChannelNormal.targets[player.uniqueId] = e.recipients.map { adaptPlayer(it) }
        TrChatEvent(ChannelNormal, player, e.message).call()
    }

    private fun checkLimits(p: ProxyPlayer, message: String): Boolean {
        if (p.hasPermission("trchat.bypass.*")) {
            return true
        }
        if (!p.hasPermission("trchat.bypass.chatlength")) {
            val limit = TrChatFiles.settings.getLong("CHAT-CONTROL.LENGTH-LIMIT", 100)
            if (message.length > limit) {
                p.sendLang("General-Too-Long", message.length, limit)
                return false
            }
        }
        if (!p.hasPermission("trchat.bypass.itemcd")) {
            val itemShowCooldown = Users.getCooldownLeft(p.uniqueId, Cooldowns.CooldownType.ITEM_SHOW)
            if (TrChatFiles.function.getStringList("GENERAL.ITEM-SHOW.KEYS").any { message.contains(it) }) {
                if (itemShowCooldown > 0) {
                    p.sendLang("Cooldowns-Item-Show", (itemShowCooldown / 1000.0).toString())
                    return false
                } else {
                    Users.updateCooldown(
                        p.uniqueId, Cooldowns.CooldownType.ITEM_SHOW,
                        (TrChatFiles.function.getDouble("GENERAL.ITEM-SHOW.COOLDOWNS") * 1000).toLong()
                    )
                }
            }
        }
        if (!p.hasPermission("trchat.bypass.chatcd")) {
            val chatCooldown = Users.getCooldownLeft(p.uniqueId, Cooldowns.CooldownType.CHAT)
            if (chatCooldown > 0) {
                p.sendLang("Cooldowns-Chat", (chatCooldown / 1000.0).toString())
                return false
            } else {
                Users.updateCooldown(
                    p.uniqueId, Cooldowns.CooldownType.CHAT,
                    (TrChatFiles.settings.getDouble("CHAT-CONTROL.COOLDOWN") * 1000).toLong()
                )
            }
        }
        if (!p.hasPermission("trchat.bypass.repeat")) {
            val lastSay = Users.getLastMessage(p.uniqueId)
            if (Strings.similarDegree(lastSay, message) > TrChatFiles.settings.getDouble("CHAT-CONTROL.ANTI-REPEAT", 0.85)) {
                p.sendLang("General-Too-Similar")
                return false
            } else {
                Users.setLastMessage(p.uniqueId, message)
            }
        }
        return true
    }
}