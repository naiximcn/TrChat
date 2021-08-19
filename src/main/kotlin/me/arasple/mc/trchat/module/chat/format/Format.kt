package me.arasple.mc.trchat.module.chat.format

import me.arasple.mc.trchat.api.TrChatHook
import me.arasple.mc.trchat.module.chat.format.objects.JsonComponent
import me.arasple.mc.trchat.module.chat.format.objects.MsgComponent
import org.bukkit.entity.Player
import taboolib.common5.Coerce
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2019/11/30 12:43
 */
open class Format(
    val priority: Int,
    val requirement: String?,
    val jsons: List<JsonComponent>,
    val msg: MsgComponent,
    val suffix: List<JsonComponent>
    ) {

    constructor(formatMap: Map<*, *>) : this(
        if (formatMap.containsKey("priority")) Coerce.toInteger(formatMap["priority"]) else Int.MAX_VALUE,
        if (formatMap.containsKey("requirement")) formatMap["requirement"].toString() else null,
        JsonComponent.loadList(formatMap["parts"]!!),
        MsgComponent(formatMap["msg"] as LinkedHashMap<*, *>),
        if (formatMap.containsKey("suffix")) JsonComponent.loadList(formatMap["suffix"]!!) else emptyList()
    )

    open fun apply(player: Player, vararg message: String): TellrawJson {
        val format = TellrawJson()
        jsons.forEach { x -> format.append(x.toTellrawJson(player)) }
        format.append(msg.toMsgTellraw(player, message[0]).also { TrChatHook.postToDynmap(player, it) })
        suffix.forEach { x -> format.append(x.toTellrawJson(player)) }
        return format
    }

}