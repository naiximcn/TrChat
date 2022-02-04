package me.arasple.mc.trchat.module.internal.command

import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.common.channel.impl.ChannelPrivateReceive
import me.arasple.mc.trchat.common.channel.impl.ChannelPrivateSend
import me.arasple.mc.trchat.util.proxy.bukkit.Players
import me.arasple.mc.trchat.util.checkMute
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandPrivateMessage
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author wlys
 * @since 2021/7/21 10:40
 */
@PlatformSide([Platform.BUKKIT])
object CommandPrivateMessage {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("msg", listOf("message", "tell", "talk", "m", "whisper", "w"), "私聊", permission = "trchat.private") {
            dynamic("player") {
                suggestion<Player> { _, _ ->
                    Players.getPlayers()
                }
                dynamic("message") {
                    suggestion<Player>(uncheck = true) { _, context ->
                        Players.getPlayers().filter {
                            it.lowercase().startsWith(context.argument(-1))
                        }
                    }
                    execute<Player> { sender, context, argument ->
                        if (sender.checkMute()) {
                            Players.getPlayerFullName(context.argument(-1))?.let {
                                TrChatEvent(ChannelPrivateSend, sender, argument, arrayOf(it)).call()
                                TrChatEvent(ChannelPrivateReceive, sender, argument, arrayOf(it)).call()
                            } ?: sender.sendLang("Command-Player-Not-Exist")
                        }
                    }
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
            incorrectCommand { sender, _, index, state ->
                when(state) {
                    1 -> {
                        when(index) {
                            -1 -> sender.sendLang("Private-Message-No-Player")
                            1 -> sender.sendLang("Private-Message-No-Message")
                        }
                    }
                    2 -> sender.sendLang("Command-Player-Not-Exist")
                }
            }
        }
    }
}
