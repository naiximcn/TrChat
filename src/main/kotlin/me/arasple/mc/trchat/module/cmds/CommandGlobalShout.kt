package me.arasple.mc.trchat.module.cmds

import me.arasple.mc.trchat.module.bungee.Bungees
import me.arasple.mc.trchat.module.channels.ChannelGlobal
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandGlobalShout
 * me.arasple.mc.trchat.module.cmds
 *
 * @author wlys
 * @since 2021/7/21 10:34
 */
@PlatformSide([Platform.BUKKIT])
object CommandGlobalShout {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("shout", listOf("all", "global"), "全服喊话", permission = "trchat.global") {
            dynamic {
                execute<Player> { sender, _, argument ->
                    if (!Bungees.isEnable) {
                        sender.sendLang("Global-Message-Not-Enable")
                        return@execute
                    }
                    ChannelGlobal.execute(sender, argument)
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
            incorrectCommand { sender, _, _, _ ->
                sender.sendLang("Global-Message-No-Message")
            }
        }
    }
}