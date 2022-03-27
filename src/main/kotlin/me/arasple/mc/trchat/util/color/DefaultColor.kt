package me.arasple.mc.trchat.util.color

import me.arasple.mc.trchat.util.Internal
import taboolib.module.chat.colored

/**
 * @author wlys
 * @since 2021/12/12 12:30
 */
@Internal
class DefaultColor(val color: String, val type: ColorType) {

    constructor(color: String) : this(
        if (color.length == 1) "§$color" else color.colored().parseHex(),
        if (Hex.GRADIENT_PATTERN.matcher(color).find()
            || Hex.RAINBOW_PATTERN.matcher(color).find()) {
            ColorType.SPECIAL
        } else {
            ColorType.NORMAL
        }
    )

    enum class ColorType {
        NORMAL, SPECIAL
    }
}