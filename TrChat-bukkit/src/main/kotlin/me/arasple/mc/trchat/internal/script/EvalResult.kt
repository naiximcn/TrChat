package me.arasple.mc.trchat.internal.script

import taboolib.common5.Coerce

/**
 * @author Arasple
 * @date 2021/1/31 11:53
 */
@JvmInline
value class EvalResult(private val any: Any?) {

    fun asBoolean(def: Boolean = false): Boolean {
        return when (any) {
            is Boolean -> any
            else -> def || Coerce.toBoolean(any)
        }
    }

    fun asString(): String {
        return any.toString()
    }

    companion object {

        val TRUE = EvalResult(true)

        val FALSE = EvalResult(false)

    }

}
