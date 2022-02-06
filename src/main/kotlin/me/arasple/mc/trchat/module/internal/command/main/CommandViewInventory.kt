package me.arasple.mc.trchat.module.internal.command.main

import me.arasple.mc.trchat.module.display.format.MsgComponent
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.platform.util.sendLang

/**
 * @author wlys
 * @since 2022/2/6 15:01
 */
@PlatformSide([Platform.BUKKIT])
object CommandViewInventory {

    @Awake(LifeCycle.ENABLE)
    fun c() {
        command("view-inventory", permissionDefault = PermissionDefault.TRUE) {
            dynamic {
                execute<Player> { sender, _, argument ->
                    MsgComponent.inventoryCache.getIfPresent(argument)?.let {
                        sender.openInventory(it)
                    } ?: kotlin.run {
                        sender.sendLang("Inventory-Show-Expired")
                    }
                }
            }
        }
    }
}