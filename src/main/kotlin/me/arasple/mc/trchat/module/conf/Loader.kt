package me.arasple.mc.trchat.module.conf

import me.arasple.mc.trchat.module.display.Channel
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.display.format.JsonComponent
import me.arasple.mc.trchat.module.display.format.MsgComponent
import me.arasple.mc.trchat.module.display.format.part.Group
import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.util.color.DefaultColor
import me.arasple.mc.trchat.util.toCondition
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.orNull
import taboolib.common5.Coerce
import java.io.File

/**
 * @author wlys
 * @since 2021/12/12 13:45
 */
object Loader {

    private val folder by lazy {
        val folder = File(getDataFolder(), "channels")

        if (!folder.exists()) {
            releaseResourceFile("channels/Normal.yml")
        }

        folder
    }

    fun load(): Int {
        Channel.channels.clear()

        filterChannelFiles(folder).forEach { load(it) }

        return Channel.channels.size
    }

    fun load(file: File): Channel {
        val conf = YamlConfiguration.loadConfiguration(file)
        val id = file.nameWithoutExtension

        val formats = conf.getMapList("Formats").map { map ->
            val condition = map["condition"]?.toString()?.toCondition()
            val priority = Coerce.asInteger(map["priority"]).orNull() ?: 100
            val prefix = parseGroups(map["prefix"] as LinkedHashMap<*, *>)
            val msg = parseMsg(map["msg"] as LinkedHashMap<*, *>)
            val suffix = parseGroups(map["suffix"] as LinkedHashMap<*, *>)

            Format(condition, priority, prefix, msg, suffix)
        }.sortedBy { it.priority }

        val settings = conf.getConfigurationSection("Options").let { map ->

        }

        return Channel(id)
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
        val hover = content["hover"]?.serialize()?.map { Hover(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val suggest = content["suggest"]?.serialize()?.map { Suggest(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val command = content["command"]?.serialize()?.map { Command(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val url = content["url"]?.serialize()?.map { Url(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val insertion = content["insertion"]?.serialize()?.map { Insertion(it.first, it.second[Property.CONDITION]?.toCondition()) }
        val copy = content["copy"]?.serialize()?.map { Copy(it.first, it.second[Property.CONDITION]?.toCondition()) }
        return JsonComponent(text, hover, suggest, command, url, insertion, copy)
    }

    private fun parseMsg(content: Map<*, *>): MsgComponent {
        val defaultColor = DefaultColor(content["default-color"]?.toString() ?: "&7")
        val hover = content["hover"]?.serialize()?.map { Hover(it.first, it.second[Property.CONDITION]?.toCondition()) }
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