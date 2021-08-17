package me.arasple.mc.trchat.module.chat.format.objects

import me.arasple.mc.trchat.util.checkCondition
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
import taboolib.module.chat.colored
import taboolib.platform.compat.replacePlaceholder

/**
 * @author Arasple
 * @date 2019/11/30 12:42
 */
open class JsonComponent {

    var requirement: String? = null
    var text: String? = null
    var hover: String? = null
    var suggest: String? = null
    var command: String? = null
    var url: String? = null

    constructor(text: String?, hover: List<String?>?, suggest: String?, command: String?, url: String?) {
        this.text = text
        this.hover = convertHoverText(hover)
        this.suggest = suggest
        this.command = command
        this.url = url
    }

    constructor(partSection: LinkedHashMap<*, *>) {
        if (partSection.containsKey("text")) {
            text = partSection["text"].toString()
        }
        if (partSection.containsKey("hover")) {
            hover = convertHoverText(partSection["hover"])
        }
        if (partSection.containsKey("suggest")) {
            suggest = partSection["suggest"].toString()
        }
        if (partSection.containsKey("command")) {
            command = partSection["command"].toString()
        }
        if (partSection.containsKey("url")) {
            url = partSection["url"].toString()
        }
        if (partSection.containsKey("requirement")) {
            requirement = partSection["requirement"].toString()
        }
    }

    fun toTellrawJson(player: Player, vararg vars: String): TellrawJson {
        return toTellrawJson(player, false, *vars)
    }

    fun toTellrawJson(player: Player, function: Boolean, vararg vars: String): TellrawJson {
        val tellraw = TellrawJson()
        if (!checkCondition(player, requirement)) {
            return tellraw
        }

        var text = text

        if (vars.size == 1 && !function) {
            text = vars[0]
        }
        if (vars.isNotEmpty()) {
            if (java.lang.Boolean.parseBoolean(vars[0])) {
                text = text!!.replace("%toplayer_name%".toRegex(), vars[2])
            }
        }
        tellraw.append(text?.replaceWithOrder(*vars)?.replacePlaceholder(player) ?: "&8[&fNull&8]".colored())
        if (hover != null) {
            tellraw.hoverText(hover!!.replaceWithOrder(*vars).replacePlaceholder(player))
        }
        if (suggest != null) {
            tellraw.suggestCommand(suggest!!.replaceWithOrder(*vars).replacePlaceholder(player))
        }
        if (command != null) {
            tellraw.runCommand(command!!.replaceWithOrder(*vars).replacePlaceholder(player))
        }
        if (url != null) {
            tellraw.openURL(url!!.replaceWithOrder(*vars).replacePlaceholder(player))
        }
        return tellraw
    }

    private fun convertHoverText(any: Any?): String {
        val hovers = if (any is List<*>) {
            any
        } else {
            return any.toString()
        }
        val hover = StringBuilder()
        hovers.forEach { l -> hover.append(l).append("\n") }
        var result = hover.toString()
        result = result.substring(0, result.length - 1)
        return result
    }

    companion object {

        fun loadList(parts: Any): List<JsonComponent> {
            val jsonComponents = mutableListOf<JsonComponent>()
            (parts as LinkedHashMap<*, *>).values.forEach { part -> jsonComponents.add(JsonComponent(part as LinkedHashMap<*, *>)) }
            return jsonComponents
        }
    }
}