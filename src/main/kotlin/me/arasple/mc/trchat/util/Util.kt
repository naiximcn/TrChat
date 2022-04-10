package me.arasple.mc.trchat.util

import me.arasple.mc.trchat.TrChat
import me.arasple.mc.trchat.module.display.ChatSession
import me.arasple.mc.trchat.module.internal.data.Database
import me.arasple.mc.trchat.module.internal.hook.HookPlugin
import me.arasple.mc.trchat.module.internal.script.Condition
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.info
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.sendLang
import taboolib.platform.util.sendLang
import java.text.SimpleDateFormat

/**
 * Util
 * me.arasple.mc.trchat.util
 *
 * @author wlys
 * @since 2021/9/12 18:11
 */
@Internal
@PlatformSide([Platform.BUKKIT])
object Util {

    lateinit var adventure: BukkitAudiences
        private set

    fun init() {
        adventure = BukkitAudiences.create(TrChat.plugin)
    }

    fun release() {
        adventure.close()
    }
}

val muteDateFormat = SimpleDateFormat()

fun Throwable.print(title: String) {
    info("§c[TrChat] §8$title")
    info("         §8${localizedMessage}")
    stackTrace.forEach {
        info("         §8$it")
    }
}

fun String.toCondition() = Condition(this)

fun Player.getSession() = ChatSession.getSession(this)

fun Player.checkMute(): Boolean {
    if (TrChat.isGlobalMuting && !hasPermission("trchat.bypass.globalmute")) {
        sendLang("General-Global-Muting")
        return false
    }
    if (this.getSession().isMuted) {
        sendLang("General-Muted", muteDateFormat.format(getDataContainer().getLong("mute_time")))
        return false
    }
    return true
}

fun Player.getDataContainer(): ConfigurationSection {
    return Database.database.pull(this)
}

fun CommandSender.sendProcessedMessage(sender: Player, component: Component) {
    if (!HookPlugin.getInteractiveChat().sendMessage(this, component)) {
        if (TrChat.paperEnv) {
            sendMessage(sender.identity(), component, MessageType.CHAT)
        } else {
            Util.adventure.sender(this).sendMessage(Identity.identity(sender.uniqueId), component, MessageType.CHAT)
        }
    }
}

fun ProxyPlayer.sendProcessedMessage(sender: Player, component: Component) {
    cast<Player>().sendProcessedMessage(sender, component)
}

fun Condition?.pass(player: Player): Boolean {
    return this?.eval(player) != false
}

fun notify(notify: Array<out ProxyCommandSender>, node: String, vararg args: Any) {
    notify.forEach { it.sendLang(node, *args) }
}