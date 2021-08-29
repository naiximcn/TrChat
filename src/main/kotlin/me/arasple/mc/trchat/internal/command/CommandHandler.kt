package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.common.channel.ChannelPrivate
import me.arasple.mc.trchat.internal.menus.MenuControlPanel
import me.arasple.mc.trchat.internal.menus.MenuFilterControl
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common5.Mirror
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandHandler
 * me.arasple.mc.trchat.internal.command
 *
 * @author wlys
 * @since 2021/8/21 12:23
 */
@PlatformSide([Platform.BUKKIT])
@CommandHeader("trchat", ["trc"], "TrChat主命令", permission = "trchat.access")
object CommandHandler {

    @CommandBody(permission = "trchat.admin", optional = true)
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

    @CommandBody(permission = "trchat.admin", optional = true)
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            TrChatFiles.reloadAll(sender)
        }
    }

    @CommandBody(permission = "trchat.admin", optional = true)
    val mirror = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            Mirror.report(sender)
        }
    }

    val spy = subCommand {
        execute<Player> { sender, _, _ ->
            val state = ChannelPrivate.switchSpy(sender)
            sender.sendLang(if (state) "Private-Message-Spy-On" else "Private-Message-Spy-Off")
        }
    }

    @CommandBody
    val main = mainCommand {
        incorrectSender { sender, _ ->
            sender.sendLang("Command-Not-Player")
        }
    }
}