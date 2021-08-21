package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.internal.menus.MenuControlPanel
import me.arasple.mc.trchat.internal.menus.MenuFilterControl
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import taboolib.platform.compat.replacePlaceholder

/**
 * CommandHandler
 * me.arasple.mc.trchat.internal.command
 *
 * @author wlys
 * @since 2021/8/21 12:23
 */
@CommandHeader("trchat", ["trc"], "TrChat主命令", permission = "trchat.access")
object CommandHandler {

    @CommandBody(permission = "trchat.controlpanel", optional = true)
    val controlPanel = subCommand {
        execute<Player> { sender, _, _ ->
            MenuControlPanel.displayFor(sender)
        }
    }

    @CommandBody(permission = "trchat.chatfilter", optional = true)
    val chatFilter = subCommand {
        execute<Player> { sender, _, _ ->
            MenuFilterControl.displayFor(sender)
        }
    }

    @CommandBody
    val main = mainCommand {
        incorrectSender { sender, _ ->
            sender.sendLang("Command-Not-Player")
        }
    }
}