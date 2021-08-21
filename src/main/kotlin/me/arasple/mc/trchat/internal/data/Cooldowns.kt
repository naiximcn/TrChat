package me.arasple.mc.trchat.internal.data

/**
 * @author Arasple
 * @date 2019/8/15 18:38
 */
class Cooldowns {

    var cooldowns = mutableListOf<Cooldown>()

    enum class CooldownType(val alias: String) {

        /**
         * Chat Cooldown Types
         */

        CHAT("Chat"),
        ITEM_SHOW("ItemShow"),
        MENTION("Mention");

    }

    class Cooldown(var id: String, var time: Long)
}