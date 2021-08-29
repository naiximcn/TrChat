package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.common.channel.impl.ChannelCustom
import me.arasple.mc.trchat.internal.data.Users
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang

/**
 * CommandStaffChat
 * me.arasple.mc.trchat.internal.command
 *
 * @author wlys
 * @since 2021/7/21 11:24
 */
@PlatformSide([Platform.BUKKIT])
object CommandChannel {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("channel", listOf("chatchannel", "trchannel"), "聊天频道", permission = "trchat.channel") {
            execute<Player> { sender, _, _ ->
                Users.removeCustomChannel(sender)
            }
            dynamic(optional = true) {
                suggestion<CommandSender> { _, _ ->
                    ChannelCustom.list.map { it.name }
                }
                execute<Player> { sender, _, argument ->
                    val channel = ChannelCustom.list.first { it.name == argument }
                    if (sender.hasPermission(channel.permission)) {
                        Users.removeCustomChannel(sender)
                        Users.setCustomChannel(sender, channel)
                        if (channel.isHint) {
                            sender.sendLang("Custom-Channel-Join", channel.name)
                        }
                    } else {
                        sender.sendLang("Command-Controller-Deny")
                    }
                }
                dynamic(optional = true) {
                    suggestion<CommandSender> { _, _ ->
                        onlinePlayers().map { it.name }
                    }
                    execute<CommandSender> { sender, context, argument ->
                        if (sender.hasPermission("trchat.admin")) {
                            val channel = ChannelCustom.list.first { it.name == context.argument(-1) }
                            val player = Bukkit.getPlayerExact(argument) ?: return@execute
                            Users.removeCustomChannel(player)
                            Users.setCustomChannel(player, channel)
                            if (channel.isHint) {
                                player.sendLang("Custom-Channel-Join", channel.name)
                            }
                        } else {
                            sender.sendLang("Command-Controller-Deny")
                        }
                    }
                }
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }
}