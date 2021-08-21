package me.arasple.mc.trchat.api.nms

import taboolib.module.nms.Packet
import taboolib.module.nms.nmsProxy

/**
 * @author Arasple
 * @date 2019/11/30 11:17
 */
abstract class PacketUtils {

    /**
     * 过滤 IChatBaseComponent 中的敏感词
     *
     * @param component 对象
     * @return 过滤后的
     */
    abstract fun filterIChatComponent(component: Any?): Any?

    abstract fun filterItem(item: Any?)

    abstract fun filterItemList(items: Any?)

    abstract fun packetToMessage(packet: Packet): String

    companion object {

        val INSTANCE = nmsProxy<PacketUtils>()
    }

}
