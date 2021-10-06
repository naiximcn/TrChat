package me.arasple.mc.trchat.util

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang

/**
 * @author Arasple
 * @date 2019/11/30 10:07
 */
fun notify(senders: Array<out ProxyCommandSender>, path: String, vararg args: Any) {
    for (sender in senders) {
        sender.sendLang(path, *args)
    }
}