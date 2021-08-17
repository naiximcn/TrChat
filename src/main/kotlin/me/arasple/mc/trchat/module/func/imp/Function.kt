package me.arasple.mc.trchat.module.func.imp

import me.arasple.mc.trchat.module.chat.format.objects.JsonComponent
import taboolib.library.configuration.MemorySection

/**
 * @author Arasple
 * @date 2019/11/30 14:17
 */
class Function(var requirement: String?, var name: String, var priority:Int, var pattern: String, var filterTextPattern: String?, var displayJson: JsonComponent) {

    constructor(name: String, funObj: MemorySection) : this(
        funObj.getString("requirement", null),
        name,
        funObj.getInt("priority", Int.MAX_VALUE),
        funObj.getString("pattern"),
        funObj.getString("text-filter", null),
        JsonComponent(((funObj["display"] as MemorySection).getValues(false) as LinkedHashMap<*, *>))
    )
}