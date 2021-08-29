package me.arasple.mc.trchat.common.channel

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.common.channel.impl.*
import me.arasple.mc.trchat.util.notify
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.mirrorNow
import taboolib.library.configuration.MemorySection

/**
 * ChatChannels
 * me.arasple.mc.trchat.common.channel
 *
 * @author wlys
 * @since 2021/8/28 20:56
 */
@PlatformSide([Platform.BUKKIT])
object ChatChannels {

    private val channels = mutableListOf<IChannel>()

    fun loadChannels(vararg notify: ProxyCommandSender) {
        val start = System.currentTimeMillis()
        channels.clear()
        ChannelCustom.list.clear()

        channels += listOf(
            ChannelNormal,
            ChannelGlobal,
            ChannelPrivateSend,
            ChannelPrivateReceive
        )
        TrChatFiles.channels.getConfigurationSection("CUSTOM").getValues(false).forEach { (name, obj) ->
            ChannelCustom.list.add(ChannelCustom(name, obj as MemorySection))
        }
        channels.addAll(ChannelCustom.list)

        notify(notify, "Plugin-Loaded-Channels", channels.size, System.currentTimeMillis() - start)
    }

    @SubscribeEvent(EventPriority.HIGHEST)
    private fun callChannel(e: TrChatEvent) {
        mirrorNow("Common:Format:${e.channel.format}") {
            e.channel.execute(e.sender, *e.message)
        }
    }
}