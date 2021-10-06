package me.arasple.mc.trchat.internal.command

import me.arasple.mc.trchat.api.event.TrChatEvent
import me.arasple.mc.trchat.common.channel.impl.ChannelGlobal
import me.arasple.mc.trchat.util.checkMute
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.command
import taboolib.module.lang.sendLang

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
            dynamic("message") {
                execute<Player> { sender, _, argument ->
                    if (sender.checkMute()) {
                        TrChatEvent(ChannelGlobal, sender, argument).call()
                    }
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