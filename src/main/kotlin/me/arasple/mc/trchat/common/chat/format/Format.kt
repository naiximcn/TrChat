package me.arasple.mc.trchat.common.chat.format

import me.arasple.mc.trchat.api.TrChatHook
import me.arasple.mc.trchat.common.chat.format.objects.JsonComponent
import me.arasple.mc.trchat.common.chat.format.objects.MsgComponent
import org.bukkit.entity.Player
import taboolib.common5.Coerce
import taboolib.module.chat.TellrawJson

/**
 * @author Arasple
 * @date 2019/11/30 12:43
 */
class Format(
    val priority: Int,
    val requirement: String?,
    val prefix: List<JsonComponent>,
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

    fun apply(player: Player, vararg message: String, forwardToDynmap: Boolean = true, privateChat: Boolean = false): TellrawJson {
        val format = TellrawJson()
        prefix.forEach { x -> format.append(x.toTellrawJson(player, *message.drop(1).toTypedArray())) }
        format.append(msg.toMsgTellraw(player, message[0], privateChat).also {
            if (forwardToDynmap) {
                TrChatHook.forwardToDynmap(player, it)
            }
        })
        suffix.forEach { x -> format.append(x.toTellrawJson(player, *message.drop(1).toTypedArray())) }
        return format
    }

}