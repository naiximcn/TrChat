package me.arasple.mc.trchat

import me.arasple.mc.trchat.common.channel.ChatChannels
import me.arasple.mc.trchat.common.chat.ChatFormats
import me.arasple.mc.trchat.common.filter.ChatFilter
import me.arasple.mc.trchat.common.function.ChatFunctions
import me.arasple.mc.trchat.internal.data.Database
import me.arasple.mc.trchat.internal.hook.HookPlugin
import me.arasple.mc.trchat.internal.proxy.Proxy
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.env.Repository.downloadToFile
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.*
import taboolib.module.lang.sendLang
import taboolib.platform.BukkitPlugin
import java.io.File
import java.io.IOException
import java.net.URL

/**
 * @author Arasple
 */
@PlatformSide([Platform.BUKKIT])
object TrChat : Plugin() {

    val plugin by lazy { BukkitPlugin.getInstance() }

    val motd = arrayOf(
        "",
        "§3  _______     §b _____  _              _   ",
        "§3 |__   __|    §b/ ____|| |           | |  ",
        "§3    | |  _ __ §b| |     | |__    __ _ | |_ ",
        "§3    `| | | '__|§b| |     | '_ \\  / _` || __|",
        "§3   | | |   | §b|____ | | | || (_| || |_ ",
        "§3    |_| |_|    §b\\_____||_| |_| \\__,_| \\__|",
    )

    var isGlobalMuting = false

    override fun onLoad() {
        motd.forEach { l -> console().sendMessage(l) }
        console().sendLang("Plugin-Loaded")

        if (!hookPlaceholderAPI()) {
            disablePlugin()
            Bukkit.shutdown()
            return
        }
        // Chat Filter
        ChatFilter.loadFilter(true, console())
        // Chat Formats
        ChatFormats.loadFormats(console())
        // Chat Functions
        ChatFunctions.loadFunctions(console())
        // Chat Channels
        ChatChannels.loadChannels(console())
        // Proxies
        Proxy.init()
    }

    override fun onEnable() {
        Database.init()
        console().sendLang("Plugin-Enabled", pluginVersion)
        HookPlugin.printInfo()
    }

    override fun onDisable() {
        console().sendLang("Plugin-Disabled")
    }

    @Awake(LifeCycle.INIT)
    fun checkVersion() {
        if (File(getDataFolder(), "logs").listFiles()?.isNotEmpty() == true) {
            if (newFile(getDataFolder(), "version").readText() == "1.90-R4") {
                return
            }
            warning("*************************************************************")
            warning(" 你好，感谢使用 TrChat (v1.90-R4)!")
            warning(" 本次更新修改了formats.yml中的parts和suffix格式(JSON)")
            warning(" 请参照新的配置文件手动迁移旧的配置文件(formats_old.yml)")
            warning(" 若无需迁移可直接删除插件目录并重新启动服务器")
            warning("")
            warning(" 确认无误后可在 \"version\" 文件中手动写入 \"1.90-R4\" 完成升级")
            warning("*************************************************************")
            File(getDataFolder(), "formats.yml").copyTo(File(getDataFolder(), "formats_old.yml"))
            releaseResourceFile("formats.yml", replace = true)
            disablePlugin()
        } else {
            newFile(getDataFolder(), "version").writeText("1.90-R4")
        }
    }

    /**
     * 检测前置 PlaceholderAPI
     * 并自动下载、重启服务器
     */
    private fun hookPlaceholderAPI(): Boolean {
        val plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")
        val jarFile = File("plugins/PlaceholderAPI.jar")
        val url = URL("https://api.spiget.org/v2/resources/6245/download")

        if (plugin == null) {
            jarFile.delete()
            console().sendLang("Plugin-Depend-Download", "PlaceholderAPI")
            try {
                downloadToFile(url, jarFile)
            } catch (e: IOException) {
                e.printStackTrace()
                console().sendLang("Plugin-Depend-Install-Failed", "PlaceholderAPI")
                return false
            }
            console().sendLang("Plugin-Depend-Installed", "PlaceholderAPI")
            return false
        }
        return true
    }
}