package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.util.getSession
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.common.platform.function.onlinePlayers
import taboolib.common5.util.parseMillis
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang

/**
 * CommandPrivateMessage
 * me.arasple.mc.trchat.module.internal.command
 *
 * @author wlys
 * @since 2021/7/21 10:40
 */
@PlatformSide([Platform.BUKKIT])
object CommandMute {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("mute", description = "禁言", usage = "/mute [player] [time]", permission = "trchat.command.mute") {
            dynamic("player") {
                suggestion<CommandSender> { _, _ ->
                    onlinePlayers().map { it.name }
                }
                dynamic("time") {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        listOf("1h", "12h", "3d", "5m")
                    }
                    execute<CommandSender> { sender, context, argument ->
                        Bukkit.getPlayer(context.argument(-1))?.let {
                            it.getSession().updateMuteTime(argument.parseMillis())
                            sender.sendLang("Mute-Muted-Player", it.name, argument)
                        } ?: sender.sendLang("Command-Player-Not-Exist")
                    }
                }
            }
            incorrectCommand { _, _, _, _ ->
                createHelper()
            }
        }
        command("muteall", listOf("globalmute"), "全员禁言", permission = "trchat.command.muteall") {
            execute<CommandSender> { sender, _, _ ->
                TrChat.isGlobalMuting = !TrChat.isGlobalMuting
                if (TrChat.isGlobalMuting) {
                    sender.sendLang("Mute-Muted-All")
                } else {
                    sender.sendLang("Mute-Cancel-Muted-All")
                }
            }
        }
    }
}