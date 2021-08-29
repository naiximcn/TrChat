package me.arasple.mc.trchat.common.channel

import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.common.channel.impl.ChannelPrivateReceive
import me.arasple.mc.trchat.common.channel.impl.ChannelPrivateSend
import me.arasple.mc.trchat.internal.service.Metrics
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.chat.ChatLogs
import me.arasple.mc.trchat.common.chat.obj.ChatType
import me.arasple.mc.trchat.internal.command.CommandReply
import me.arasple.mc.trchat.internal.proxy.Proxy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.util.*

/**
 * @author Arasple
 * @date 2019/8/17 22:57
 */
@PlatformSide([Platform.BUKKIT])
object ChannelPrivate {

    private val spying = mutableListOf<UUID>()

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("spy", permission = "trchat.admin", permissionMessage = console().asLangText("General-No-Permission")) {
            execute<Player> { sender, _, _ ->
                val state = switchSpy(sender)
                sender.sendLang(if (state) "Private-Message-Spy-On" else "Private-Message-Spy-Off")
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }

    fun execute(from: Player, to: String, message: String) {
        TrChatEvent(ChannelPrivateSend, from, message, to).call()
        TrChatEvent(ChannelPrivateReceive, from, message, to).call()

        // Spy
        spying.forEach { spy ->
            val spyPlayer = Bukkit.getPlayer(spy)
            if (spyPlayer != null && spyPlayer.isOnline) {
                spyPlayer.sendLang("Private-Message-Spy-Format", from.name, to, message)
            }
        }
        console().sendLang("Private-Message-Spy-Format", from.name, to, message)
        ChatLogs.logPrivate(from.name, to, message)
        CommandReply.lastMessageFrom[from.uniqueId] = to
        Metrics.increase(0)
    }

    fun switchSpy(player: Player): Boolean {
        if (!spying.contains(player.uniqueId)) {
            spying.add(player.uniqueId)
        } else {
            spying.remove(player.uniqueId)
        }
        return spying.contains(player.uniqueId)
    }

    fun isSpying(player: Player): Boolean {
        return spying.contains(player.uniqueId)
    }
}