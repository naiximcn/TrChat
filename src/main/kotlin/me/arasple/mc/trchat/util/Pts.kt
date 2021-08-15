package me.arasple.mc.trchat.util

import taboolib.common.util.replaceWithOrder
import java.util.regex.Pattern

/**
 * @author Arasple
 * @date 2019/11/30 14:30
 */
fun replacePattern(s: String, pattern: String, textPattern: String?, replacement: String): String {
    var string = s
    val matcher = Pattern.compile(pattern).matcher(string)
    while (matcher.find()) {
        val str = matcher.group()
        val m = if (textPattern != null) Pattern.compile(textPattern).matcher(str) else null
        val rep = replacement.replaceWithOrder(if (textPattern == null) str else if (m != null && m.find()) m.group() else str)
        string = string.replace(str.toRegex(), rep)
    }
    return string
}