package me.arasple.mc.trchat.module.cmds

import me.arasple.mc.trchat.module.chat.MessageTransmit.releaseTransmit
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang

/**
 * CommandIgnore
 * me.arasple.mc.trchat.module.cmds
 *
 * @author wlys
 * @since 2021/8/11 12:08
 */
@PlatformSide([Platform.BUKKIT])
object CommandRecall {

//    @Awake(LifeCycle.ENABLE) TODO
    fun c() {
        command("recall", description = "撤回消息", permission = "trchat.recall") {
            execute<Player> { sender, _, _ ->
                sender.releaseTransmit()
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }
}