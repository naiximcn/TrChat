package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.internal.proxy.bungee.Bungees
import me.arasple.mc.trchat.common.channels.ChannelGlobal
import me.arasple.mc.trchat.internal.data.Users
import me.arasple.mc.trchat.internal.listener.ListenerChatEvent
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
 * me.arasple.mc.trchat.internal.command
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
                    if (ListenerChatEvent.isGlobalMuted && !sender.hasPermission("trchat.bypass.globalmute")) {
                        sender.sendLang("General-Global-Muting")
                        return@execute
                    }
                    if (Users.isMuted(sender)) {
                        sender.sendLang("General-Muted")
                        return@execute
                    }
                    if (!Bungees.isEnabled) {
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