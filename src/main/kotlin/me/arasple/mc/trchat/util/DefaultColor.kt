package me.arasple.mc.trchat.util

import me.arasple.mc.trmenu.util.parseGradients
import me.arasple.mc.trmenu.util.parseHex
import me.arasple.mc.trmenu.util.parseRainbow

/**
 * @author wlys
 * @since 2021/12/12 12:30
 */
class DefaultColor(val color: String, val type: ColorType) {

    constructor(color: String) : this(
        if (color.length == 1) "§$color" else color,
        if (color.length <= 2) ColorType.LEGACY else ColorType.HEX
    )

    fun colored(string: String): String = type.transfer(color, string)

    enum class ColorType(val transfer: String.(String) -> String) {
        LEGACY({
            this + it
        }),
        HEX({
            (this + it).parseHex().parseGradients().parseRainbow()
        })
    }
}