package me.arasple.mc.trchat.module.internal.listener

import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.module.display.channel.Channel
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.util.sendLang

/**
 * @author Arasple, wlys
 * @date 2020/1/16 21:41
 */
@PlatformSide([Platform.BUKKIT])
object ListenerCommand {

    @SubscribeEvent(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        val player = e.player
        var cmd = e.message.removePrefix("/").trimIndent()

        if (!Functions.CONF.getBoolean("General.Command-Controller.Enable", true) || cmd.isEmpty()) {
            return
        }
        val command = cmd.split(" ")

        val mCmd = Bukkit.getCommandAliases().entries.firstOrNull { (_, value) ->
            value.any { it.equals(command[0], ignoreCase = true) }
        }
        cmd = if (mCmd != null) mCmd.key + cmd.removePrefix(mCmd.key) else cmd

        val controller = Functions.commandController.get().entries.firstOrNull { it.key.matches(cmd) }?.value

        val condition = controller?.first
        if (condition != null && !condition.eval(player)) {
            e.isCancelled =  true
            player.sendLang("Command-Controller-Deny")
            return
        }

        val baffle = controller?.second
        if (baffle != null && !baffle.hasNext(player.name) && !player.hasPermission("trchat.bypass.cmdcooldown")) {
            e.isCancelled =  true
            player.sendLang("Command-Controller-Cooldown")
            return
        }

        val channel = Channel.channels
            .firstOrNull { it.bindings.command?.any { c -> c.equals(command[0], ignoreCase = true) } == true } ?: return
        e.isCancelled = true

        if (command.size > 1) {
            channel.execute(player, cmd.substringAfter(' '))
        } else {
            Channel.join(player, channel)
        }
    }
}