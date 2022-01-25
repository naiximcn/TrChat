package me.arasple.mc.trchat.module.display.format.part

import me.arasple.mc.trchat.module.script.Condition
import me.arasple.mc.trmenu.util.Regexs
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson
import taboolib.platform.compat.replacePlaceholder

/**
 * @author wlys
 * @since 2022/1/21 23:21
 */
class Suggest(override val content: String, override val condition: Condition?, val copy: Boolean) : Part() {

    override val dynamic by lazy { Regexs.containsPlaceholder(content) }

    override fun process(tellraw: TellrawJson, player: Player) {
        if (copy) {
            if (dynamic) {
                tellraw.copyOrSuggest(content.replacePlaceholder(player))
            } else {
                tellraw.copyOrSuggest(content)
            }
        } else {
            if (dynamic) {
                tellraw.suggestCommand(content.replacePlaceholder(player))
            } else {
                tellraw.suggestCommand(content)
            }
        }
    }

    private fun TellrawJson.copyOrSuggest(text: String) {
        try {
            copyToClipboard(text)
        } catch (_: NoSuchFieldError) {
            suggestCommand(text)
        }
    }
}