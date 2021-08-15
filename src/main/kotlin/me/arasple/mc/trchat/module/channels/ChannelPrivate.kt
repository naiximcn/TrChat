package me.arasple.mc.trchat.module.channels

import com.google.common.collect.Lists
import me.arasple.mc.trchat.module.bstats.Metrics
import me.arasple.mc.trchat.module.chat.ChatFormats
import me.arasple.mc.trchat.module.chat.obj.ChatType
import me.arasple.mc.trchat.module.cmds.CommandReply
import me.arasple.mc.trchat.module.logs.ChatLogs
import me.arasple.mc.trchat.util.Bungees
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.command.command
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.util.*

/**
 * @author Arasple
 * @date 2019/8/17 22:57
 */
object ChannelPrivate {

    private val spying: MutableList<UUID> = Lists.newArrayList()

    @Awake(LifeCycle.ENABLE)
    fun init() {
        command("spy", permission = "trchat.admin", permissionMessage = console().asLangText("General-No-Permission")) {
            execute<Player> { sender, _, _ ->
                val state = switchSpy(sender)
                sender.sendLang(if (state) "Private-Message-Spy-On" else "Private-Message-Spy-Off")
            }
            incorrectSender { sender, _ ->
                sender.sendLang("Command-Not-Player")
            }
        }
    }

    fun execute(from: Player, to: String, message: String) {
        val sender = ChatFormats.getFormat(ChatType.PRIVATE_SEND, from)!!.apply(from, message, from.name, to)
        val receiver = ChatFormats.getFormat(ChatType.PRIVATE_RECEIVE, from)!!.apply(from, message, from.name, to)

        val toPlayer = Bukkit.getPlayerExact(to)
        if (toPlayer == null || !toPlayer.isOnline) {
            val raw: String = ComponentSerializer.toString(*receiver.componentsAll.toTypedArray())
            Bungees.sendBungeeData(from, "TrChat", "SendRaw", to, raw)
        } else {
            receiver.sendTo(getProxyPlayer(to)!!)
            getProxyPlayer(to)!!.sendLang("Private-Message-Receive", from.name)
        }
        sender.sendTo(adaptPlayer(from))

        val spyFormat = console().asLangText("Private-Message-Spy-Format", from.name, to, message)

        spying.forEach { spy ->
            val spyPlayer = Bukkit.getPlayer(spy)
            if (spyPlayer != null && spyPlayer.isOnline) {
                spyPlayer.sendMessage(spyFormat)
            }
        }
        Bukkit.getConsoleSender().sendMessage(spyFormat)
        ChatLogs.logPrivate(from.name, to, message)
        CommandReply.lastMessageFrom[from.uniqueId] = to
        Metrics.increase(0)
    }

    fun switchSpy(player: Player): Boolean {
        if (!spying.contains(player.uniqueId)) {
            spying.add(player.uniqueId)
        } else {
            spying.remove(player.uniqueId)
        }
        return spying.contains(player.uniqueId)
    }

    fun isSpying(player: Player): Boolean {
        return spying.contains(player.uniqueId)
    }
}