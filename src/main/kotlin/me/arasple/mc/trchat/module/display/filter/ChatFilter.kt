package me.arasple.mc.trchat.module.display.filter

import com.google.gson.JsonParser
import me.arasple.mc.trchat.api.config.Filter
import me.arasple.mc.trchat.module.display.filter.processer.FilteredObject
import me.arasple.mc.trchat.module.display.filter.processer.WordContext
import me.arasple.mc.trchat.module.display.filter.processer.WordFilter
import me.arasple.mc.trchat.module.display.filter.processer.WordType
import me.arasple.mc.trchat.module.internal.service.Metrics
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

    lateinit var context: WordContext
    var skip = 2
    var replacement = '*'

    private val CLOUD_LAST_UPDATE = mutableMapOf<String, String>()
    private var CLOUD_URL = listOf<String>()
    private val CLOUD_WORDS = mutableSetOf<String>()

    @Schedule(delay = (20 * 120).toLong(), period = (30 * 60 * 20).toLong(), async = true)
    fun asyncRefreshCloud() {
        loadCloudFilter()
    }

    /**
     * 加载聊天过滤器
     *
     * @param updateCloud 是否更新云端词库
     * @param notify      接受通知反馈
     */
    fun loadFilter(updateCloud: Boolean, vararg notify: ProxyCommandSender) {

        // 初始化本地配置
        context = WordContext().also {
            it.addWord(Filter.CONF.getStringList("Local"), WordType.BLACK)
            it.addWord(Filter.CONF.getStringList("WhiteList"), WordType.WHITE)
        }
        skip = Filter.CONF.getInt("Skip")
        replacement = Filter.CONF.getString("Replacement")!![0]

        // 更新云端词库
        if (updateCloud && Filter.CONF.getBoolean("Cloud-Thesaurus.Enabled")) {
            CLOUD_URL = Filter.CONF.getStringList("Cloud-Thesaurus.Urls")
            submit(async = true) {
                loadCloudFilter(*notify)
            }
        }
        notify(notify, "Plugin-Loaded-Filter-Local", Filter.CONF.getStringList("Local").size)
    }

    /**
     * 加载云端聊天敏感词库
     *
     * @param notify 接受通知反馈
     */
    private fun loadCloudFilter(vararg notify: ProxyCommandSender) {
        if (CLOUD_URL.isEmpty()) {
            return
        }
        val collected = mutableListOf<String>()
        CLOUD_URL.forEach {
            collected += catchCloudThesaurus(it, *notify)
        }
        CLOUD_WORDS += collected
        if (CLOUD_WORDS.isEmpty()) {
            notify(notify, "Plugin-Failed-Load-Filter-Cloud")
        } else {
            context.addWord(CLOUD_WORDS, WordType.BLACK)
        }
    }

    /**
     * 抓取云端聊天敏感词库
     *
     * @param url    尝试 URL 序号
     * @param notify 接受通知反馈
     */
    private fun catchCloudThesaurus(url: String, vararg notify: ProxyCommandSender): List<String> {
        val whitelist = Filter.CONF.getStringList("Cloud-Thesaurus.Ignored")
        val collected = mutableListOf<String>()

        return kotlin.runCatching {
            URL(url).openStream().use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val database = JsonParser().parse(readFully(bufferedInputStream, StandardCharsets.UTF_8)).asJsonObject
                    if (!database.has("lastUpdateDate") || !database.has("words")) {
                        error("Wrong database json object")
                    }

                    val lastUpdateDate = database["lastUpdateDate"].asString
                    CLOUD_LAST_UPDATE[url] = when (CLOUD_LAST_UPDATE[url]) {
                        null -> lastUpdateDate
                        lastUpdateDate -> return emptyList()
                        else -> lastUpdateDate
                    }
                    database["words"].asJsonArray.forEach {
                        val word = it.asString
                        if (whitelist.none { w -> w.equals(word, ignoreCase = true) }) {
                            collected.add(word)
                        }
                    }
                }
            }
            notify(notify, "Plugin-Loaded-Filter-Cloud", collected.size, url, CLOUD_LAST_UPDATE[url]!!)
            collected
        }.getOrElse {
            it.printStackTrace()
            emptyList()
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
        return if (execute) {
            mirrorNow("Handler:Filter:doFilter") {
                WordFilter(context).replace(string, skip, replacement).also {
                    Metrics.increase(1, it.sensitiveWords)
                }
            }
        } else {
            FilteredObject(string, 0)
        }
    }
}