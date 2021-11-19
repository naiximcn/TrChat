package me.arasple.mc.trchat.common.channel

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.common.channel.impl.*
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.service.Metrics
import me.arasple.mc.trchat.util.notify
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.mirrorNow
import taboolib.library.configuration.MemorySection
import kotlin.system.measureTimeMillis

/**
 * ChatChannels
 * me.arasple.mc.trchat.common.channel
 *
 * @author wlys
 * @since 2021/8/28 20:56
 */
@PlatformSide([Platform.BUKKIT])
object ChatChannels {

    private val channels = mutableListOf<ChannelAbstract>()

    private var default: ChannelCustom? = null

    fun loadChannels(vararg notify: ProxyCommandSender) {
        measureTimeMillis {
            channels.clear()
            ChannelCustom.list.clear()

            channels += listOf(
                ChannelNormal,
                ChannelGlobal,
                ChannelPrivateSend,
                ChannelPrivateReceive
            )
            TrChatFiles.channels.getConfigurationSection("CUSTOM")?.getValues(false)?.forEach { (name, obj) ->
                ChannelCustom.list.add(ChannelCustom(name, obj as MemorySection))
            }
            channels.addAll(ChannelCustom.list)
            default = ChannelCustom.of(TrChatFiles.channels.getString("DEFAULT-CUSTOM-CHANNEL", null))
        }.also { notify(notify, "Plugin-Loaded-Channels", channels.size, it) }
    }

    internal object ChannelListener {

        @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
        fun callChannel(e: TrChatEvent) {
            mirrorNow("Common:Channel:${e.channel.format}") {
                e.channel.execute(e.sender, e.message, e.args)
            }
            if (e.channel != ChannelPrivateSend) {
                Metrics.increase(0)
            }
        }

        @SubscribeEvent
        fun autoJoinChannel(e: PlayerJoinEvent) {
            val player = e.player
            if (default != null
                && Users.getCustomChannel(player) == null
                && !player.hasPermission("trchat.bypass.defaultchannel")
                && player.hasPermission(default!!.permission)
            ) {
                ChannelCustom.join(player, default!!)
            }
        }
    }
}