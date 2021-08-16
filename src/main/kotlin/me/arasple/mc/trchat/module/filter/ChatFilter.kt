package me.arasple.mc.trchat.module.filter

import com.google.gson.JsonParser
import me.arasple.mc.trchat.api.TrChatFiles.filter
import me.arasple.mc.trchat.module.filter.processer.Filter
import me.arasple.mc.trchat.module.filter.processer.FilteredObject
import me.arasple.mc.trchat.util.notify
import taboolib.common.LifeCycle
import taboolib.common.env.DependencyDownloader.readFully
import taboolib.common.platform.*
import taboolib.common.platform.function.submit
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * @author Arasple
 * @date 2019/10/12 20:52
 */
@PlatformSide([Platform.BUKKIT])
object ChatFilter {

    private var CHATFILTER_CLOUD_LAST_UPDATE: String? = null
    private val CHATFILTER_CLOUD_URL = arrayOf(
        "https://arasple.oss-cn-beijing.aliyuncs.com/plugins/TrChat/database.json",
        "https://raw.githubusercontent.com/Arasple/TrChat-Cloud/master/database.json"
    )

    @Awake(LifeCycle.ENABLE)
    fun asyncRefreshCloud() {
        submit(delay = 20 * 120, period = 30 * 60 * 20) {
            loadCloudFilter(0)
        }
    }

    /**
     * 加载聊天过滤器
     *
     * @param updateCloud 是否更新云端词库
     * @param notify      接受通知反馈
     */
    fun loadFilter(updateCloud: Boolean, vararg notify: ProxyCommandSender) {
        // 初始化本地配置
        Filter.setSensitiveWord(filter.getStringList("LOCAL"))
        Filter.setPunctuations(filter.getStringList("IGNORED-PUNCTUATIONS"))
        Filter.setReplacement(filter.getString("REPLACEMENT")[0])

        // 更新云端词库
        if (updateCloud && filter.getBoolean("CLOUD-THESAURUS.ENABLE", true)) {
            loadCloudFilter(0, *notify)
        } else {
            notify(notify, "Plugin-Loaded-Filter-Local", filter.getStringList("LOCAL").size)
        }
    }

    /**
     * 加载云端聊天敏感词库
     *
     * @param url    尝试 URL 序号
     * @param notify 接受通知反馈
     */
    private fun loadCloudFilter(url: Int, vararg notify: ProxyCommandSender) {
        submit(async = true) {
            val whitelist = filter.getStringList("CLOUD-THESAURUS.WHITELIST")
            val collected: MutableList<String> = ArrayList()
            val lastUpdateDate: String

            try {
                val inputStream = URL(CHATFILTER_CLOUD_URL[url]).openStream()
                val bufferedInputStream = BufferedInputStream(inputStream)
                val database = JsonParser().parse(readFully(bufferedInputStream, StandardCharsets.UTF_8)).asJsonObject
                if (!database.has("lastUpdateDate") || !database.has("words")) {
                    throw NullPointerException("Wrong database json object")
                }

                lastUpdateDate = database.get("lastUpdateDate").asString
                CHATFILTER_CLOUD_LAST_UPDATE = when (CHATFILTER_CLOUD_LAST_UPDATE) {
                    null -> lastUpdateDate
                    lastUpdateDate -> return@submit
                    else -> lastUpdateDate
                }
                database.get("words").asJsonArray.iterator().forEachRemaining { i ->
                    val word: String = i.asString
                    if (whitelist.stream().noneMatch { w -> w.equals(word, ignoreCase = true) }) {
                        collected.add(word)
                    }
                }
            } catch (e: Throwable) {
                if (url == 0) {
                    loadCloudFilter(url + 1, *notify)
                } else {
                    notify(notify, "Plugin-Loaded-Filter-Local", filter.getStringList("LOCAL").size)
                    notify(notify, "Plugin-Failed-Load-Filter-Cloud")
                }
                return@submit
            }
            notify(notify, "Plugin-Loaded-Filter-Local", filter.getStringList("LOCAL").size)
            notify(notify, "Plugin-Loaded-Filter-Cloud", collected.size, url, lastUpdateDate)
            Filter.addSensitiveWord(collected)
        }
    }

    /**
     * 过滤一个字符串
     *
     * @param string  待过滤字符串
     * @param execute 是否真的过滤
     * @return 过滤后的字符串
     */
    fun filter(string: String, execute: Boolean = true): FilteredObject {
        return if (!execute) {
            FilteredObject(string, 0)
        } else {
            Filter.doFilter(string)
        }
    }
}