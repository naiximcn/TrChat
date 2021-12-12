package me.arasple.mc.trchat.module.display.format

import me.arasple.mc.trchat.api.Functions
import me.arasple.mc.trchat.util.DefaultColor
import me.arasple.mc.trchat.util.Variables
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.TellrawJson
import taboolib.module.nms.getI18nName
import java.util.function.Function

/**
 * @author wlys
 * @since 2021/12/12 13:46
 */
class MsgComponent(var defaultColor: DefaultColor?, hover: String?, suggest: String?, command: String?, url: String?, copy: String?) :
    JsonComponent(null, "", hover, suggest, command, url, copy) {

    fun serialize(player: Player, message: String): TellrawJson {

    }

    companion object {

        val functions = mutableListOf<Function<String, String>>()

        init {
            functions.add {

                Variables(it, Functions.itemShow.getString("")).element.joinToString {

                }
            }
        }

        private fun ItemStack.getName(player: Player): String {
            return if ((Functions.itemShow.getBoolean("Origin-Name", false)
                        || itemMeta == null) || !itemMeta!!.hasDisplayName()
            ) {
                getI18nName(player)
            } else {
                itemMeta!!.displayName
            }
        }
    }
}