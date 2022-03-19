package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.api.TrChatAPI
import me.arasple.mc.trchat.module.display.format.part.json.*
import me.arasple.mc.trchat.module.display.function.Function
import me.arasple.mc.trchat.module.display.function.Function.Companion.replaceRegex
import me.arasple.mc.trchat.module.display.function.InventoryShow
import me.arasple.mc.trchat.module.display.function.ItemShow
import me.arasple.mc.trchat.module.display.function.Mention
import me.arasple.mc.trchat.util.color.DefaultColor
import me.arasple.mc.trchat.util.color.MessageColors
import me.arasple.mc.trchat.util.legacy
import me.arasple.mc.trchat.util.pass
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import taboolib.common.util.VariableReader

/**
 * @author wlys
 * @since 2021/12/12 13:46
 */
class MsgComponent(
    var defaultColor: DefaultColor,
    hover: Hover?,
    suggest: List<Suggest>?,
    command: List<Command>?,
    url: List<Url>?,
    insertion: List<Insertion>?,
    copy: List<Copy>?
) : JsonComponent(null, hover, suggest, command, url, insertion, copy) {

    fun serialize(player: Player, msg: String, disabledFunctions: List<String>): TextComponent {
        val component = Component.text()

        var message = msg
        if (!disabledFunctions.contains("Item-Show")) {
            message = ItemShow.replaceMessage(message, player)
        }
        if (!disabledFunctions.contains("Mention")) {
            message = Mention.replaceMessage(message, player)
        }
        if (!disabledFunctions.contains("Inventory-Show")) {
            message = InventoryShow.replaceMessage(message)
        }
        Function.functions.filter { it.condition.pass(player) && !disabledFunctions.contains(it.id) }.forEach {
            message = message.replaceRegex(it.regex, it.filterTextPattern, "{{${it.id}:{0}}}")
        }

        val defaultColor = MessageColors.catchDefaultMessageColor(player, defaultColor)

        parser.readToFlatten(message).forEach { part ->
            if (part.isVariable) {
                val args = part.text.split(":", limit = 2)
                when (val id = args[0]) {
                    "ITEM" -> {
                        component.append(ItemShow.createComponent(player, args[1].toInt()))
                    }
                    "MENTION" -> {
                        component.append(Mention.createComponent(player, args[1]))
                    }
                    "INVENTORY" -> {
                        component.append(InventoryShow.createComponent(player))
                    }
                    else -> {
                        Function.functions.firstOrNull { it.id == id }?.let {
                            component.append(it.displayJson.toTextComponent(player, args[1]))
                            it.action?.let { action -> TrChatAPI.eval(player, action) }
                        }
                    }
                }
            } else {
                component.append(toTextComponent(player, MessageColors.defaultColored(defaultColor, player, message)))
            }
        }
        return component.build()
    }

    override fun toTextComponent(player: Player, vararg vars: String): TextComponent {
        val message = vars[0]
        var component = legacy(message)

        component = hover?.process(component, player, *vars) ?: component
        component = suggest?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = command?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = url?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = insertion?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component
        component = copy?.firstOrNull { it.condition.pass(player) }?.process(component, player, *vars) ?: component

        return component
    }

    companion object {

        private val parser = VariableReader()

    }
}