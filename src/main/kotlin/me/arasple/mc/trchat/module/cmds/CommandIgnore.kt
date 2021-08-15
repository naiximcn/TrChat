package me.arasple.mc.trchat.module.cmds

import me.arasple.mc.trchat.module.data.Users
import me.arasple.mc.trchat.util.Players
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandIgnore
 * me.arasple.mc.trchat.module.cmds
 *
 * @author wlys
 * @since 2021/8/11 12:08
 */
@PlatformSide([Platform.BUKKIT])
object CommandIgnore {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("ignore", description = "屏蔽玩家", permission = "trchat.ignore") {
            dynamic {
                suggestion<Player> { sender, _ ->
                    Players.getPlayers().filter { it != sender.name }
                }
                execute<Player> { sender, _, argument ->
                    if (Users.getIgnoredList(sender).contains(argument)) {
                        Users.removeIgnored(sender, argument)
                        sender.sendLang("Ignore-Add", argument)
                    } else {
                        Users.addIgnored(sender, argument)
                        sender.sendLang("Ignore-Remove", argument)
                    }
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
            incorrectCommand { sender, _, _, state ->
                when (state) {
                    1 -> sender.sendLang("Ignore-No-Player")
                    2 -> sender.sendLang("Command-Player-Not-Exist")
                }
            }
        }
    }
}