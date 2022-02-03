package me.arasple.mc.trchat.module.conf

import me.arasple.mc.trchat.module.display.Channel
import me.arasple.mc.trchat.module.display.format.Format
import me.arasple.mc.trchat.module.display.format.part.Hover
import me.arasple.mc.trchat.module.display.format.part.Text
import me.arasple.mc.trchat.util.toCondition
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.asList
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
            val priority = Coerce.asInteger(map["priority"]).orElse(null) ?: 100
            val prefix = (map["prefix"] as LinkedHashMap<*, *>).forEach { (_, content) ->
                content as LinkedHashMap<*, *>
                val text = Property.serialize(content["text"] ?: "null").map { Text(it.first, it.second[Property.CONDITION]?.toCondition()) }
                val hover = content["hover"]?.serialize()?.map { Hover(it.first.asList()) }
            }

            Format(condition, priority, prefix)
        }

        return Channel(id)
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