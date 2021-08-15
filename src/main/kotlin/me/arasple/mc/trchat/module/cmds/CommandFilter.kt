package me.arasple.mc.trchat.module.cmds

import me.arasple.mc.trchat.module.menus.MenuFilterControl
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang

/**
 * CommandFilter
 * me.arasple.mc.trchat.module.cmds
 *
 * @author wlys
 * @since 2021/7/21 10:29
 */
@PlatformSide([Platform.BUKKIT])
object CommandFilter {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("filter", listOf("filters", "chatfilter", "trfilter"), "敏感词过滤器", permission = "trchat.filter") {
            execute<Player> { sender, _, _ ->
                MenuFilterControl.displayFor(sender)
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }
}