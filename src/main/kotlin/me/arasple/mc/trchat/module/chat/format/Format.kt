package me.arasple.mc.trchat.module.chat.format

import me.arasple.mc.trchat.module.chat.format.objects.JsonComponent
import me.arasple.mc.trchat.module.chat.format.objects.MsgComponent
import org.apache.commons.lang.math.NumberUtils
import org.bukkit.entity.Player
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2019/11/30 12:43
 */
open class Format(
    private val priority: Int,
    var requirement: String?,
    var jsons: List<JsonComponent>,
    var msg: MsgComponent
    ) {

    constructor(formatMap: Map<*, *>) : this(
        if (formatMap.containsKey("priority")) NumberUtils.toInt(formatMap["priority"].toString(), 0) else Int.MIN_VALUE,
        if (formatMap.containsKey("requirement")) formatMap["requirement"].toString() else null,
        JsonComponent.loadList(formatMap["parts"]!!),
        MsgComponent(formatMap["msg"] as LinkedHashMap<*, *>)
    )

    open fun apply(player: Player, vararg message: String): TellrawJson {
        val format = TellrawJson()
        jsons.forEach { x -> format.append(x.toTellrawJson(player)) }
        format.append(msg.toMsgTellraw(player, message[0]))
        return format
    }

}