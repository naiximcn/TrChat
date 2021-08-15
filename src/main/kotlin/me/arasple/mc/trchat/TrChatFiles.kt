package me.arasple.mc.trchat

import me.arasple.mc.trchat.module.chat.ChatFormats
import me.arasple.mc.trchat.module.filter.ChatFilter
import me.arasple.mc.trchat.module.func.ChatFunctions
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
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
        FileWatcher.INSTANCE.addSimpleListener(
            filter.file, { ChatFilter.loadFilter(false) }, true
        )
        FileWatcher.INSTANCE.addSimpleListener(
            formats.file, { ChatFormats.loadFormats() }, true
        )
        FileWatcher.INSTANCE.addSimpleListener(
            function.file, { ChatFunctions.loadFunctions() }, true
        )
    }
}