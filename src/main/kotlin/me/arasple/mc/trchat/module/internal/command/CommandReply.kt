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
 * CommandReply
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author wlys
 * @since 2021/7/21 11:14
 */
@PlatformSide([Platform.BUKKIT])
object CommandReply {

    val lastMessageFrom = HashMap<String, String>()

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("reply", listOf("r"), "回复私聊", permission = "trchat.private") {
            dynamic("message") {
                execute<Player> { sender, _, argument ->
                    if (sender.checkMute()) {
                        if (lastMessageFrom.containsKey(sender.name)) {
                            Players.getPlayerFullName(lastMessageFrom[sender.name]!!)?.let {
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
            incorrectCommand { sender, _, _, _ ->
                sender.sendLang("Private-Message-No-Message")
            }
        }
    }
}