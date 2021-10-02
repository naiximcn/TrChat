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

    private val CLOUD_LAST_UPDATE = mutableMapOf<String, String>()
    private var CLOUD_URL = mutableListOf<String>()
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
        Filter.setSensitiveWord(filter.getStringList("LOCAL"))
        Filter.setPunctuations(filter.getStringList("IGNORED-PUNCTUATIONS"))
        Filter.setReplacement(filter.getString("REPLACEMENT")[0])

        // 更新云端词库
        if (updateCloud && filter.getBoolean("CLOUD-THESAURUS.ENABLE", false)) {
            CLOUD_URL = filter.getStringList("CLOUD-THESAURUS.URL")
            submit(async = true) {
                loadCloudFilter(*notify)
            }
        }
        notify(notify, "Plugin-Loaded-Filter-Local", filter.getStringList("LOCAL").size)
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
        for (i in CLOUD_URL.indices) {
            collected += catchCloudThesaurus(i, *notify)
        }
        CLOUD_WORDS += collected
        if (CLOUD_WORDS.isEmpty()) {
            notify(notify, "Plugin-Failed-Load-Filter-Cloud")
        } else {
            Filter.addSensitiveWord(CLOUD_WORDS.toList())
        }
    }

    /**
     * 抓取云端聊天敏感词库
     *
     * @param url    尝试 URL 序号
     * @param notify 接受通知反馈
     */
    private fun catchCloudThesaurus(url: Int, vararg notify: ProxyCommandSender): List<String> {
        val address = CLOUD_URL[url]
        val whitelist = filter.getStringList("CLOUD-THESAURUS.WHITELIST")
        val collected = mutableListOf<String>()

        return kotlin.runCatching {
            URL(address).openStream().use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val database = JsonParser().parse(readFully(bufferedInputStream, StandardCharsets.UTF_8)).asJsonObject
                    if (!database.has("lastUpdateDate") || !database.has("words")) {
                        error("Wrong database json object")
                    }

                    val lastUpdateDate = database["lastUpdateDate"].asString
                    CLOUD_LAST_UPDATE[address] = when (CLOUD_LAST_UPDATE[address]) {
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
            notify(notify, "Plugin-Loaded-Filter-Cloud", collected.size, url, CLOUD_LAST_UPDATE[address]!!)
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
        return mirrorNow("Common:Filter:doFilter") {
            Filter.doFilter(string, execute)
        }
    }
}