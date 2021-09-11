package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.common.channel.ChannelPrivate.execute
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.listener.ListenerChatEvent
import me.arasple.mc.trchat.internal.proxy.bukkit.Players
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.util.*

/**
 * CommandReply
 * me.arasple.mc.trchat.internal.command
 *
 * @author wlys
 * @since 2021/7/21 11:14
 */
@PlatformSide([Platform.BUKKIT])
object CommandReply {

    val lastMessageFrom = HashMap<UUID, String>()

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("reply", listOf("r"), "回复私聊", permission = "trchat.private") {
            dynamic("message") {
                execute<Player> { sender, _, argument ->
                    if (ListenerChatEvent.isGlobalMuting && !sender.hasPermission("trchat.bypass.globalmute")) {
                        sender.sendLang("General-Global-Muting")
                        return@execute
                    }
                    if (Users.isMuted(sender)) {
                        sender.sendLang("General-Muted")
                        return@execute
                    }
                    if (lastMessageFrom.containsKey(sender.uniqueId)) {
                        Players.getPlayerFullName(lastMessageFrom[sender.uniqueId]!!)?.let {
                            execute(sender, it, argument)
                        } ?: sender.sendLang("Command-Player-Not-Exist")
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