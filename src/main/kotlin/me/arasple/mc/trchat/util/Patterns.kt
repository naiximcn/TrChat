package me.arasple.mc.trchat.util

import taboolib.common.util.replaceWithOrder

/**
 * @author Arasple
 * @date 2019/11/30 14:30
 */
fun replacePattern(s: String, pattern: String, textPattern: String?, replacement: String): String {
    var string = s
    val matcher = pattern.toPattern().matcher(string)
    while (matcher.find()) {
        val str = matcher.group()
        val m = textPattern?.toPattern()?.matcher(str)
        val rep = replacement.replaceWithOrder(if (textPattern == null) str else if (m != null && m.find()) m.group() else str)
        string = string.replace(str, rep)
    }
    return string
}