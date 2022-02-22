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

    @ConfigNode("General.Item-Show.Cooldown", "function.yml")
    val itemShowDelay = ConfigNodeTransfer<Double, Baffle> { Baffle.of((this * 1000).toLong(), TimeUnit.MILLISECONDS) }

    @ConfigNode("General.Item-Show.Keys", "function.yml")
    val itemShowKeys = ConfigNodeTransfer<List<String>, List<Regex>> { map { Regex("$it(-[1-9])?") } }

    @ConfigNode("General.Mention.Cooldown", "function.yml")
    val mentionDelay = ConfigNodeTransfer<Double, Baffle> { Baffle.of((this * 1000).toLong(), TimeUnit.MILLISECONDS) }

    @ConfigNode("General.Inventory-Show.Cooldown", "function.yml")
    val inventoryShowDelay = ConfigNodeTransfer<Double, Baffle> { Baffle.of((this * 1000).toLong(), TimeUnit.MILLISECONDS) }

    @ConfigNode("General.Item-Show", "function.yml")
    lateinit var itemShow: ConfigurationSection

    @ConfigNode("General.Mention", "function.yml")
    lateinit var mention: ConfigurationSection

    @ConfigNode("General.Inventory-Show", "function.yml")
    lateinit var inventoryShow: ConfigurationSection
}