package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.common.channel.ChannelPrivate
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.listener.ListenerChatEvent
import me.arasple.mc.trchat.internal.proxy.bukkit.Players
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.util.*

/**
 * CommandPrivateMessage
 * me.arasple.mc.trchat.internal.command
 *
 * @author wlys
 * @since 2021/7/21 10:40
 */
@PlatformSide([Platform.BUKKIT])
object CommandPrivateMessage {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("msg", listOf("message", "tell", "talk", "m"), "私聊", permission = "trchat.private") {
            dynamic {
                suggestion<Player> { _, _ ->
                    Players.getPlayers()
                }
                dynamic {
                    suggestion<Player>(uncheck = true) { _, context ->
                        Players.getPlayers().filter {
                            it.lowercase(Locale.getDefault()).startsWith(context.argument(-1)!!)
                        }
                    }
                    execute<Player> { sender, context, argument ->
                        if (ListenerChatEvent.isGlobalMuting && !sender.hasPermission("trchat.bypass.globalmute")) {
                            sender.sendLang("General-Global-Muting")
                            return@execute
                        }
                        if (Users.isMuted(sender)) {
                            sender.sendLang("General-Muted")
                            return@execute
                        }
                        Players.getPlayerFullName(context.argument(-1)!!)?.let {
                            ChannelPrivate.execute(sender, it, argument)
                        } ?: sender.sendLang("Command-Player-Not-Exist")
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