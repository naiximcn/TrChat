package me.arasple.mc.trchat.internal.menus

import me.arasple.mc.trchat.internal.data.Users
import org.bukkit.entity.Player
import taboolib.common.platform.function.onlinePlayers
import taboolib.common5.Coerce
import taboolib.library.xseries.XMaterial
import taboolib.module.nms.inputSign
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.inventoryCenterSlots
import taboolib.platform.util.sendLang

/**
 * MenuControlPanel
 * me.arasple.mc.trchat.internal.menus
 *
 * @author wlys
 * @since 2021/8/20 20:47
 */
object MenuControlPanel {

    fun displayFor(player: Player) {
        player.openMenu<Linked<Player>> {
            rows(6)
            slots(inventoryCenterSlots)
            elements { onlinePlayers().map { it.cast() } }
            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f上一页" }
                } else {
                    buildItem(XMaterial.ARROW) { name = "§8上一页" }
                }
            }
            setPreviousPage(47) { _, hasPreviousPage ->
                if (hasPreviousPage) {
                    buildItem(XMaterial.SPECTRAL_ARROW) { name = "§f下一页" }
                } else {
                    buildItem(XMaterial.ARROW) { name = "§8下一页" }
                }
            }
            onGenerate { _, element, _, _ ->
                buildItem(XMaterial.PLAYER_HEAD) {
                    skullOwner = element.name
                    name = "&e${element.name}"
                    lore += listOf(
                        "",
                        "&a➦ 点击管理",
                        ""
                    )
                    colored()
                }
            }
            onClick { event, element ->
                each(event.clicker, element)
            }
        }
    }

    private fun each(player: Player, target: Player) {
        player.openMenu<Basic> {
            rows(3)
            map("", "P#M######")
            set('P', XMaterial.PLAYER_HEAD) {
                skullOwner = target.name
                name = "&e${target.name}"
                lore += listOf(
                    "",
                    "&a上次发言: &7${Users.getLastMessage(target.uniqueId)}",
                    ""
                )
                colored()
            }
            set('M', XMaterial.REDSTONE) {
                name = "&c禁言"
                colored()
            }
            onClick(lock = true) { clickEvent ->
                when (clickEvent.slot) {
                    'M' -> {
                        player.inputSign(arrayOf("", "↑请输入禁言时间(分钟)", "输入0解除禁言")) {
                            if (Coerce.asInteger(it[0]).isPresent) {
                                Users.updateMuteTime(target, Coerce.toLong(it[0]) * 60)
                                player.sendLang("Plugin-Done")
                            } else {
                                player.sendLang("Plugin-Failed")
                            }
                        }
                    }
                }
            }
        }
    }
}