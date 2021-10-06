package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.common.chat.ChatMessage
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang

/**
 * CommandIgnore
 * me.arasple.mc.trchat.internal.command
 *
 * @author wlys
 * @since 2021/8/11 12:08
 */
@PlatformSide([Platform.BUKKIT])
object CommandRemoveMessage {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("removemessage", description = "删除消息", permission = "trchat.removemessage") {
            dynamic {
                suggestion<Player> { sender, _ ->
                    ChatMessage.MESSAGES[sender.uniqueId]?.mapNotNull { it.message }
                }
                execute<Player> { _, _, argument ->
                    ChatMessage.removeMessage(argument)
                    ChatMessage.releaseMessage()
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }
}