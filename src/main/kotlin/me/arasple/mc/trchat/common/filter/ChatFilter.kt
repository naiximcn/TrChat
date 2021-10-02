package me.arasple.mc.trchat.common.filter

import com.google.gson.JsonParser
import me.arasple.mc.trchat.api.TrChatFiles.filter
import me.arasple.mc.trchat.common.filter.processer.Filter
import me.arasple.mc.trchat.common.filter.processer.FilteredObject
import me.arasple.mc.trchat.util.notify
import taboolib.common.env.DependencyDownloader.readFully
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.submit
import taboolib.common5.mirrorNow
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * @author Arasple
 * @date 2019/10/12 20:52
 */
@PlatformSide([Platform.BUKKIT])
object ChatFilter {

    private var CLOUD_LAST_UPDATE: String? = null
    private val CLOUD_URL = mutableListOf<String>()
    private var CLOUD_LAST_WORDS = listOf<String>()

    @Schedule(delay = 20 * 120, period = 30 * 60 * 20, async = true)
    fun asyncRefreshCloud() {
        loadCloudFilter(0)
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
        if (updateCloud && filter.getBoolean("CLOUD-THESAURUS.ENABLE", false)) {
            CLOUD_URL.clear()
            CLOUD_URL += filter.getStringList("CLOUD-THESAURUS.URL")
            submit(async = true) {
                Filter.addSensitiveWord(loadCloudFilter(0, *notify).let {
                    if (it.isEmpty()) {
                        CLOUD_LAST_WORDS
                    } else {
                        CLOUD_LAST_WORDS = it
                        it
                    }
                })
            }
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
    private fun loadCloudFilter(url: Int, vararg notify: ProxyCommandSender): List<String> {
        if (CLOUD_URL.isEmpty()) {
            return emptyList()
        }
        val whitelist = filter.getStringList("CLOUD-THESAURUS.WHITELIST")
        val collected = mutableSetOf<String>()

        return kotlin.runCatching {
            URL(CLOUD_URL[url]).openStream().use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val database = JsonParser().parse(readFully(bufferedInputStream, StandardCharsets.UTF_8)).asJsonObject
                    if (!database.has("lastUpdateDate") || !database.has("words")) {
                        error("Wrong database json object")
                    }

                    val lastUpdateDate = database.get("lastUpdateDate").asString
                    CLOUD_LAST_UPDATE = when (CLOUD_LAST_UPDATE) {
                        null -> lastUpdateDate
                        lastUpdateDate -> return emptyList()
                        else -> lastUpdateDate
                    }
                    database.get("words").asJsonArray.forEach {
                        val word = it.asString
                        if (whitelist.none { w -> w.equals(word, ignoreCase = true) }) {
                            collected.add(word)
                        }
                    }
                }
            }
            if ((url + 1) < CLOUD_URL.size) {
                collected += loadCloudFilter(url + 1)
            }
            collected.toList()
        }.getOrElse {
            it.printStackTrace()
            emptyList()
        }.also {
            if (it.isNotEmpty()) {
                notify(notify, "Plugin-Loaded-Filter-Local", filter.getStringList("LOCAL").size)
                notify(notify, "Plugin-Loaded-Filter-Cloud", collected.size, CLOUD_URL.size, CLOUD_LAST_UPDATE!!)
            } else {
                notify(notify, "Plugin-Loaded-Filter-Local", filter.getStringList("LOCAL").size)
                notify(notify, "Plugin-Failed-Load-Filter-Cloud")
            }
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
        return mirrorNow("Common:Filter:doFilter") {
            Filter.doFilter(string, execute)
        }
    }
}