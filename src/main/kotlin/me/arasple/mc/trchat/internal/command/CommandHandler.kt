package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.api.TrChatFiles
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.filter.ChatFilter
import me.arasple.mc.trchat.common.function.ChatFunctions
import me.arasple.mc.trchat.internal.menus.MenuControlPanel
import me.arasple.mc.trchat.internal.menus.MenuFilterControl
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang

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
        execute<ProxyCommandSender> { sender, _, _ ->
            TrChatFiles.formats.reload()
            ChatFormats.loadFormats(sender)
            TrChatFiles.filter.reload()
            ChatFilter.loadFilter(true, sender)
            TrChatFiles.function.reload()
            ChatFunctions.loadFunctions(sender)
        }
    }

    @CommandBody
    val main = mainCommand {
        incorrectSender { sender, _ ->
            sender.sendLang("Command-Not-Player")
        }
    }
}