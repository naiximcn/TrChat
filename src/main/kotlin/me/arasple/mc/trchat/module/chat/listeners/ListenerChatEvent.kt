package me.arasple.mc.trchat.module.chat.listeners

import me.arasple.mc.trchat.Metrics
import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.module.channels.ChannelGlobal
import me.arasple.mc.trchat.module.channels.ChannelStaff
import me.arasple.mc.trchat.module.channels.ChannelStaff.isInStaffChannel
import me.arasple.mc.trchat.module.chat.ChatFormats
import me.arasple.mc.trchat.module.chat.obj.ChatType
import me.arasple.mc.trchat.module.data.Cooldowns
import me.arasple.mc.trchat.module.data.Users
import me.arasple.mc.trchat.module.logs.ChatLogs
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Strings
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * @author Arasple, wlys
 * @date 2019/11/30 12:10
 */
@PlatformSide([Platform.BUKKIT])
object ListenerChatEvent {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChat(e: AsyncPlayerChatEvent) {
        val player = e.player
        if (TrChatFiles.settings.getStringList("GENERAL.DISABLED-WORLDS").contains(player.world.name)) {
            e.isCancelled = false
            return
        }
        if (Users.isMuted(player)) {
            e.isCancelled = true
            player.sendLang("GENERAL.MUTE")
            return
        }
        // STAFF
        if (isInStaffChannel(player)) {
            e.isCancelled = true
            ChannelStaff.execute(player, e.message)
            return
        }
        if (!checkLimits(adaptPlayer(player), e.message)) {
            e.isCancelled = true
            return
        }
        // GLOBAL
        if (TrChatFiles.channels.getBoolean("FORCE-GLOBAL", false)
            || e.message.startsWith(TrChatFiles.channels.getString("FORCE-GLOBAL-PREFIX", "!all"))) {
            e.isCancelled = true
            ChannelGlobal.execute(player, e.message.replace(TrChatFiles.channels.getString("FORCE-GLOBAL-PREFIX", "!all"), ""))
            return
        }
        // NORMAL
        val format = ChatFormats.getFormat(ChatType.NORMAL, player)
        if (format != null) {
            e.isCancelled = true
            val message = format.apply(player, e.message)
            onlinePlayers().filterNot { Users.getIgnoredList(it.cast()).contains(player.name) }.forEach {
                message.sendTo(it)
            }
            message.sendTo(console())
            ChatLogs.log(player, e.message)
            Users.formattedMessage[player.uniqueId] = ComponentSerializer.toString(*message.componentsAll.toTypedArray())
            Metrics.increase(0)
        }
    }

    private fun checkLimits(p: ProxyPlayer, message: String): Boolean {
        if (!p.hasPermission("trchat.bypass.*")) {
            val limit = TrChatFiles.settings.getLong("CHAT-CONTROL.LENGTH-LIMIT", 100)
            if (message.length > limit) {
                p.sendLang("General-Too-Long", message.length, limit)
                return false
            }
        }
        if (!p.hasPermission("trchat.bypass.itemcd")) {
            val itemShowCooldown = Users.getCooldownLeft(p.uniqueId, Cooldowns.CooldownType.ITEM_SHOW)
            if (TrChatFiles.function.getStringList("GENERAL.ITEM-SHOW.KEYS").any { sequence ->
                        message.contains(sequence!!)
                    }) {
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