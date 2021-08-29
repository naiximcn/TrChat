package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.listener.ListenerChatEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.command.command
import taboolib.common.platform.function.onlinePlayers
import taboolib.common5.Coerce
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandPrivateMessage
 * me.arasple.mc.trchat.internal.command
 *
 * @author wlys
 * @since 2021/7/21 10:40
 */
@PlatformSide([Platform.BUKKIT])
object CommandMute {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("mute", description = "禁言", usage = "/mute 玩家 禁言时间", permission = "trchat.mute") {
            dynamic {
                suggestion<CommandSender> { _, _ ->
                    onlinePlayers().map { it.name }
                }
                dynamic {
                    suggestion<CommandSender>(uncheck = true) { _, _ ->
                        listOf("1", "5", "10", "60")
                    }
                    restrict<CommandSender> { _, _, argument ->
                        Coerce.asInteger(argument).isPresent
                    }
                    execute<CommandSender> { sender, context, argument ->
                        Bukkit.getPlayer(context.argument(-1)!!)?.let {
                            Users.updateMuteTime(it, Coerce.toLong(argument) * 60)
                            sender.sendLang("Mute-Muted-Player", it.name, argument)
                        } ?: sender.sendLang("Command-Player-Not-Exist")
                    }
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
            incorrectCommand { sender, _, index, state ->
                when (state) {
                    1 -> {
                        when (index) {
                            -1 -> sender.sendLang("Mute-No-Player")
                            0 -> sender.sendLang("Mute-Time-Not-Specified")
                        }
                    }
                    2 -> {
                        when (index) {
                            0 -> sender.sendLang("Command-Player-Not-Exist")
                            1 -> sender.sendLang("Mute-Time-Not-Specified")
                        }
                    }
                }
            }
        }
        command("muteall", listOf("globalmute"), "全员禁言", "trchat.mute") {
            execute<CommandSender> { sender, _, _ ->
                ListenerChatEvent.isGlobalMuting = !ListenerChatEvent.isGlobalMuting
                if (ListenerChatEvent.isGlobalMuting) {
                    sender.sendLang("Mute-Muted-All")
                } else {
                    sender.sendLang("Mute-Cancel-Muted-All")
                }
            }
        }
    }
}