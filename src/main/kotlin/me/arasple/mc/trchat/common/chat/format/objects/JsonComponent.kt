package me.arasple.mc.trchat.common.chat.format.objects

import me.arasple.mc.trchat.internal.script.Condition
import me.arasple.mc.trchat.util.coloredAll
import org.bukkit.entity.Player
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.TellrawJson
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
    var copy: String? = null

    constructor(text: String?, hover: List<String?>?, suggest: String?, command: String?, url: String?, copy: String?) {
        this.text = text
        this.hover = convertHoverText(hover)
        this.suggest = suggest
        this.command = command
        this.url = url
        this.copy = copy
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
        if (partSection.containsKey("copy")) {
            copy = partSection["copy"].toString()
        }
    }

    fun toTellrawJson(player: Player, vararg vars: String): TellrawJson {
        return toTellrawJson(player, false, *vars)
    }

    fun toTellrawJson(player: Player, function: Boolean, vararg vars: String): TellrawJson {
        val tellraw = TellrawJson()
        if (!Condition.eval(player, requirement).asBoolean()) {
            return tellraw
        }

        var text = text

        if (vars.size == 1 && !function) {
            text = vars[0]
        }
        if (vars.isNotEmpty()) {
            if (vars[0].toBoolean()) {
                text = text!!.replace("%toplayer_name%", vars[1])
            }
        }
        tellraw.append(text?.replaceWithOrder(*vars)?.replacePlaceholder(player)?.coloredAll() ?: "§8[§fNull§8]")
        hover?.let { tellraw.hoverText(it.replaceWithOrder(*vars).replacePlaceholder(player).coloredAll()) }
        suggest?.let { tellraw.suggestCommand(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        command?.let { tellraw.runCommand(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        url?.let { tellraw.openURL(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        copy?.let { tellraw.copyToClipboard(it.replaceWithOrder(*vars).replacePlaceholder(player)) }
        return tellraw
    }

    private fun convertHoverText(any: Any?): String {
        return if (any is List<*>) {
            any.joinToString("\n") { it.toString() }
        } else {
            any.toString()
        }
    }

    companion object {

        fun loadList(parts: Any): List<JsonComponent> {
            val jsonComponents = mutableListOf<JsonComponent>()
            (parts as LinkedHashMap<*, *>).values.forEach { part -> jsonComponents.add(JsonComponent(part as LinkedHashMap<*, *>)) }
            return jsonComponents
        }
    }
}