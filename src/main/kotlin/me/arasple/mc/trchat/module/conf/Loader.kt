package me.arasple.mc.trchat.module.conf

import me.arasple.mc.trchat.api.config.Functions
import me.arasple.mc.trchat.module.display.channel.*
import me.arasple.mc.trchat.module.display.channel.Target
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.display.format.JsonComponent
import me.arasple.mc.trchat.module.display.format.MsgComponent
import me.arasple.mc.trchat.module.display.format.part.Group
import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.util.color.DefaultColor
import me.arasple.mc.trchat.util.toCondition
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.orNull
import taboolib.common5.Coerce
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.util.getMap
import taboolib.module.lang.sendLang
import java.io.File
import kotlin.system.measureTimeMillis

/**
 * @author wlys
 * @since 2021/12/12 13:45
 */
@RuntimeDependencies(
    RuntimeDependency("!net.kyori:adventure-api:4.9.1", test = "!net.kyori.adventure.Adventure"),
    RuntimeDependency("!net.kyori:adventure-platform-bukkit:4.0.1", test = "!net.kyori.adventure.platform.bukkit.BukkitAudience"),
    RuntimeDependency("!net.kyori:adventure-platform-bungeecord:4.0.1", test = "!net.kyori.adventure.platform.bungeecord.BukkitAudience")
)
@PlatformSide([Platform.BUKKIT])
object Loader {

    private val folder by lazy {
        val folder = File(getDataFolder(), "channels")

        if (!folder.exists()) {
            releaseResourceFile("channels/Normal.yml")
            releaseResourceFile("channels/Global.yml")
            releaseResourceFile("channels/Staff.yml")
            releaseResourceFile("channels/Private.yml")
        }

        folder
    }

    fun loadChannels(sender: ProxyCommandSender) {
        measureTimeMillis { loadChannels() }.let {
            sender.sendLang("Plugin-Loaded-Channels", Channel.channels.size, it)
        }
    }

    fun loadChannels(): Int {
        Channel.channels.clear()

        filterChannelFiles(folder).forEach {
            Channel.channels.add(loadChannels(it))
        }

        return Channel.channels.size
    }

    fun loadChannels(file: File): Channel {
        val conf = YamlConfiguration.loadConfiguration(file)
        val id = file.nameWithoutExtension

        val settings = conf.getConfigurationSection("Options")!!.let { section ->
            val joinPermission = section.getString("Join-Permission")
            val speakCondition = section.getString("Speak-Condition")?.toCondition()
            val target = section.getString("Target", "ALL")!!.uppercase().split(";").let {
                val distance = it.getOrNull(1)?.toInt() ?: -1
                Target(Target.Range.valueOf(it[0]), distance)
            }
            val autoJoin = section.getBoolean("Auto-Join", true)
            val proxy = section.getBoolean("Proxy", false)
            val ports = section.getString("Ports")?.split(";")?.map { it.toInt() }
            val disabledFunctions = section.getStringList("Disabled-Functions")
            ChannelSettings(joinPermission, speakCondition, target, autoJoin, proxy, ports, disabledFunctions)
        }
        val private = conf.getBoolean("Options.Private", false)

        val bindings = conf.getConfigurationSection("Bindings")?.let {
            val prefix = if (!private) it.getStringList("Prefix") else null
            val command = it.getStringList("Command")
            ChannelBindings(prefix, command)
        } ?: ChannelBindings(null, null)

        if (private) {
            val sender = conf.getMapList("Sender").map { map ->
                val condition = map["condition"]?.toString()?.toCondition()
                val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
                val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
                val suffix = parseGroups(map["suffix"] as LinkedHashMap<*, *>)
                Format(condition, priority, prefix, msg, suffix)
            }.sortedBy { it.priority }
            val receiver = conf.getMapList("Receiver").map { map ->
                val condition = map["condition"]?.toString()?.toCondition()
                val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
                val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
                val suffix = parseGroups(map["suffix"] as LinkedHashMap<*, *>)
                Format(condition, priority, prefix, msg, suffix)
            }.sortedBy { it.priority }

            return PrivateChannel(id, settings, bindings, sender, receiver)
        } else {
            val formats = conf.getMapList("Formats").map { map ->
                val condition = map["condition"]?.toString()?.toCondition()
                val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
                val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
                val suffix = parseGroups(map["suffix"] as LinkedHashMap<*, *>)
                Format(condition, priority, prefix, msg, suffix)
            }.sortedBy { it.priority }

            return Channel(id, settings, bindings, formats)
        }
    }

    fun loadFunctions(sender: ProxyCommandSender) {
        measureTimeMillis { loadFunctions() }.let {
            sender.sendLang("Plugin-Loaded-Functions", Function.functions.size + 3, it)
        }
    }

    fun loadFunctions() {
        Function.functions.clear()

        val customs = Functions.CONF.getMap<String, ConfigurationSection>("Custom")
        val functions = customs.map { (id, map) ->
            val condition = map.getString("condition")?.toCondition()
            val priority = map.getInt("priority", 100)
            val regex = map.getString("pattern")!!.toRegex()
            val filterTextPattern = map.getString("text-filter")?.toPattern()
            val displayJson = parseJSON(map.getConfigurationSection("display")!!.toMap())
            val action = map["action"]?.toString()

            Function(id, condition, priority, regex, filterTextPattern, displayJson, action)
        }.sortedBy { it.priority }

        Function.functions.addAll(functions)
    }

    private fun parseGroups(map: LinkedHashMap<*, *>): Map<String, List<Group>> {
        return map.map { (id, content) ->
            id as String
            when (content) {
                is Map<*, *> -> {
                    val condition = content["condition"]?.toString()?.toCondition()
                    id to listOf(Group(condition, 100, parseJSON(content)))
                }
                is List<*> -> {
                    id to content.map {
                        it as LinkedHashMap<*, *>
                        val condition = it["condition"]?.toString()?.toCondition()
                        val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
                        Group(condition, priority, parseJSON(it))
                    }.sortedBy { it.priority }
                }
                else -> error("Unexpected group: $content")
            }
        }.toMap()
    }

    private fun parseJSON(content: Map<*, *>): JsonComponent {
        val text = Property.serialize(content["text"] ?: "null").map { Text(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val hover = content["hover"]?.serialize()?.associate { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Hover(it) }
        val suggest = content["suggest"]?.serialize()?.map { Suggest(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val command = content["command"]?.serialize()?.map { Command(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val url = content["url"]?.serialize()?.map { Url(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val insertion = content["insertion"]?.serialize()?.map { Insertion(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val copy = content["copy"]?.serialize()?.map { Copy(it.first, it.second[Property.CONDITION]?.toCondition()) }
        return JsonComponent(text, hover, suggest, command, url, insertion, copy)
    }

    private fun parseMsg(content: Map<*, *>): MsgComponent {
        val defaultColor = DefaultColor(content["default-color"]?.toString() ?: "&7")
        val hover = content["hover"]?.serialize()?.associate { it.first to it.second[Property.CONDITION]?.toCondition() }?.let { Hover(it) }
        val suggest = content["suggest"]?.serialize()?.map { Suggest(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val command = content["command"]?.serialize()?.map { Command(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val url = content["url"]?.serialize()?.map { Url(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val insertion = content["insertion"]?.serialize()?.map { Insertion(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val copy = content["copy"]?.serialize()?.map { Copy(it.first, it.second[Property.CONDITION]?.toCondition()) }
        return MsgComponent(defaultColor, hover, suggest, command, url, insertion, copy)
    }

    private fun filterChannelFiles(file: File): List<File> {
        return mutableListOf<File>().apply {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    addAll(filterChannelFiles(it))
                }
            } else if (file.extension.equals("yml", true)) {
                add(file)
            }
        }
    }

    private fun Any.serialize(): List<Pair<String, Map<Property, String>>> {
        return Property.serialize(this)
    }
}