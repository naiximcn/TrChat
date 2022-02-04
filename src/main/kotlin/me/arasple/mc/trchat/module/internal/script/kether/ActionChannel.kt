package me.arasple.mc.trchat.module.internal.script.kether

import me.arasple.mc.trchat.common.channel.impl.ChannelCustom
import me.arasple.mc.trchat.internal.data.Users
import taboolib.library.kether.LocalizedException
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture

/**
 * KetherChannel
 * me.arasple.mc.trchat.internal.script.kether
 *
 * @author wlys
 * @since 2021/8/29 15:44
 */
class ActionChannel(val symbol: Symbol, val channel: String?): ScriptAction<Void>() {

    enum class Symbol {

        JOIN, QUIT
    }

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        val s = frame.script()
        if (s.sender == null) {
            error("No sender selected.")
        }
        when (symbol) {
            Symbol.JOIN -> ChannelCustom.join(s.sender!!.cast(), channel!!)
            Symbol.QUIT -> Users.removeCustomChannel(s.sender!!.cast())
        }
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        @KetherParser(["channel"], namespace = "trchat")
        fun parser() = scriptParser {
            val symbol = when(val type = it.nextToken().lowercase()) {
                "join" -> Symbol.JOIN
                "quit" -> Symbol.QUIT
                "leave" -> Symbol.QUIT
                else -> throw LocalizedException.of("load-error.custom", "Unknown channel operator $type")
            }
            ActionChannel(symbol, if (symbol == Symbol.JOIN) it.nextToken() else null)
        }
    }
}