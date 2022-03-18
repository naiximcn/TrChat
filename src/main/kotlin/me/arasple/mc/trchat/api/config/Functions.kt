package me.arasple.mc.trchat.api.config

import me.arasple.mc.trchat.module.conf.Loader
import me.arasple.mc.trchat.module.conf.Property
import me.arasple.mc.trchat.module.internal.script.Condition
import me.arasple.mc.trchat.util.toCondition
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common5.Baffle
import taboolib.common5.util.parseMillis
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.configuration.Configuration
import java.util.concurrent.TimeUnit

/**
 * @author wlys
 * @since 2021/12/12 11:40
 */
@PlatformSide([Platform.BUKKIT])
object Functions {

    @Config("function.yml", autoReload = true)
    lateinit var CONF: Configuration
        private set

    @Awake(LifeCycle.LOAD)
    fun init() {
        CONF.onReload {
            Loader.loadFunctions()
        }
    }

    @ConfigNode("General.Command-Controller.List", "function.yml")
    val commandController = ConfigNodeTransfer<List<*>, Map<Regex, Pair<Condition?, Baffle?>>> {
        associate { string ->
            val (cmd, property) = Property.from(string!!.toString())
            val baffle = property[Property.COOLDOWN]?.toFloat()?.let {
                Baffle.of((it * 1000).toLong(), TimeUnit.MILLISECONDS)
            }
            val condition = property[Property.CONDITION]?.toCondition()
            val mCmd = Bukkit.getCommandAliases().entries.firstOrNull { (_, value) ->
                value.any { it.equals(cmd.split(" ")[0], ignoreCase = true) }
            }
            val key = if (mCmd != null) mCmd.key + cmd.removePrefix(mCmd.key) else cmd
            val regex = if (property[Property.EXACT].toBoolean()) {
                Regex("(?i)$key\$")
            } else {
                Regex("(?i)$key.*")
            }
            regex to Pair(condition, baffle)
        }
    }
}