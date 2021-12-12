package me.arasple.mc.trchat.module.display

import me.arasple.mc.trchat.module.display.format.Format

/**
 * @author wlys
 * @since 2021/12/11 22:27
 */
class Channel(
    val id: String,
    val formats: List<Format>
) {

    companion object {

        val channels = mutableListOf<Channel>()
    }
}