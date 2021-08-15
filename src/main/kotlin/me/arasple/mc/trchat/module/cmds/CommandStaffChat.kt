package me.arasple.mc.trchat.module.cmds

import me.arasple.mc.trchat.module.channels.ChannelStaff.execute
import me.arasple.mc.trchat.module.channels.ChannelStaff.switchStaff
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandStaffChat
 * me.arasple.mc.trchat.module.cmds
 *
 * @author wlys
 * @since 2021/7/21 11:24
 */
@PlatformSide([Platform.BUKKIT])
object CommandStaffChat {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("staff", listOf("staffchannel"), "管理频道", permission = "trchat.staff") {
            dynamic(true) {
                execute<Player> { sender, _, argument ->
                    execute(sender, argument)
                }
            }
            execute<Player> { sender, _, _ ->
                val state = switchStaff(sender)
                sender.sendLang(if (state) "Staff-Channel-Join" else "Staff-Channel-Quit")
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }
}