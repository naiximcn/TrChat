package me.arasple.mc.trchat.common.chat.obj

/**
 * @author Arasple
 * @date 2019/11/30 13:23
 */
enum class ChatType {

    /**
     * 常规聊天
     */
    NORMAL,

    /**
     * 全局喊话
     */
    GLOBAL,

    /**
     * 玩家私聊
     */
    PRIVATE_SEND,
    PRIVATE_RECEIVE,

    /**
     * 管理交流
     */
    STAFF;

    val isPrivate
        get() = this == PRIVATE_RECEIVE || this == PRIVATE_SEND

}
