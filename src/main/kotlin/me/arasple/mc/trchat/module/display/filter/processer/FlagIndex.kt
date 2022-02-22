package me.arasple.mc.trchat.module.display.filter.processer

/**
 * 敏感词标记
 *
 * @author minghu.zhang
 */
class FlagIndex {
    /**
     * 标记结果
     */
    var isFlag = false

    /**
     * 是否黑名单词汇
     */
    var isWhiteWord = false

    /**
     * 标记索引
     */
    var index: List<Int>? = null
}