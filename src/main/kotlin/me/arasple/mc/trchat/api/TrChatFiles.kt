package me.arasple.mc.trchat.api

import me.arasple.mc.trchat.common.channel.ChatChannels
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.filter.ChatFilter
import me.arasple.mc.trchat.common.function.ChatFunctions
import org.bukkit.command.CommandSender
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common5.FileWatcher
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile

/**
 * @author Arasple, wlys
 * @date 2019/11/30 9:59
 */
@PlatformSide([Platform.BUKKIT])
object TrChatFiles {

    @Config("settings.yml", migrate = true)
    lateinit var settings: SecuredFile
        private set

    @Config("formats.yml", migrate = true)
    lateinit var formats: SecuredFile
        private set

    @Config("filter.yml", migrate = true)
    lateinit var filter: SecuredFile
        private set

    @Config("function.yml", migrate = true)
    lateinit var function: SecuredFile
        private set

    @Config("channels.yml", migrate = true)
    lateinit var channels: SecuredFile
        private set

    @Awake(LifeCycle.ENABLE)
    fun init() {
        FileWatcher.INSTANCE.addSimpleListener(filter.file) {
            filter.reload()
            ChatFilter.loadFilter(false, console())
        }
        FileWatcher.INSTANCE.addSimpleListener(formats.file) {
            formats.reload()
            ChatFormats.loadFormats(console())
        }
        FileWatcher.INSTANCE.addSimpleListener(function.file) {
            function.reload()
            ChatFunctions.loadFunctions(console())
        }
        FileWatcher.INSTANCE.addSimpleListener(channels.file) {
            channels.reload()
            ChatChannels.loadChannels(console())
        }
    }

    fun reloadAll(notify: CommandSender) {
        formats.reload()
        ChatFormats.loadFormats(adaptCommandSender(notify))
        filter.reload()
        ChatFilter.loadFilter(true, adaptCommandSender(notify))
        function.reload()
        ChatFunctions.loadFunctions(adaptCommandSender(notify))
        channels.reload()
        ChatChannels.loadChannels(adaptCommandSender(notify))
    }
}