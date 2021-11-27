package me.arasple.mc.trchat.internal.database

import org.bukkit.entity.Player
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.database.ColumnOptionSQLite
import taboolib.module.database.ColumnTypeSQLite
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.io.File

/**
 * @Author sky
 * @Since 2020-08-14 14:46
 */
class DatabaseLocal {

    val host = File(getDataFolder(), "data.db").getHost()

    val table = Table("trchat", host) {
        add {
            name("user")
            type(ColumnTypeSQLite.TEXT, 36) {
                options(ColumnOptionSQLite.PRIMARY_KEY)
            }
        }
        add {
            name("data")
            type(ColumnTypeSQLite.TEXT)
        }
    }

    val dataSource = host.createDataSource()

    init {
        table.workspace(dataSource) { createTable(true) }.run()
    }

    fun pull(player: Player): ConfigurationSection? {
        return table.workspace(dataSource) {
            select { where { "user" eq player.name } }
        }.firstOrNull {
            Configuration.loadFromString(getString("data"))
        }
    }

    fun release(player: Player) {
        table.workspace(dataSource) {
            delete { where { "user" eq player.name } }
        }.run()
    }
}