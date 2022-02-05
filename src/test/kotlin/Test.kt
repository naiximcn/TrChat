import me.arasple.mc.trchat.module.display.filter.processer.WordContext
import me.arasple.mc.trchat.module.display.filter.processer.WordFilter
import me.arasple.mc.trchat.module.display.filter.processer.WordType

/**
 * @author wlys
 * @since 2022/2/4 22:14
 */
object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val filter = WordFilter(WordContext().also {
            it.addWord(listOf("abC"), WordType.BLACK)
        })
        println(filter.include("aBc"))
    }
}