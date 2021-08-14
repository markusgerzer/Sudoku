import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.tests.ViewsForTesting
import storage.Storage
import storage.Storage.dataStorage
import kotlin.test.Test

class PersistentDataTest: ViewsForTesting(), Storage.Storable {
    init {
        Storage.nativeStorage = stage.storage
    }
    override val storageKey = "PersistentDataTest"
    var int by dataStorage(4, false)
    var string by dataStorage("...", false)
    var intList by dataStorage(listOf(0, 1, 2), false)
    var anyList by dataStorage<Any>(listOf(4, "dd", 5.9), false)
    var map by dataStorage(mapOf<String, Any>(), false)

    var int99 by dataStorage(2, false)

    @Test
    fun test() {
        println(int); int = 7; println(int); println()
        println(string); string = "blabla"; println(string); println()
        println(intList); intList = listOf(1, 2, 3); println(intList); println()
        println(anyList); anyList = listOf(1, 4.2, "bla"); println(anyList); println()
        println(map); map = mapOf("s1" to 3, "s2" to "bla", "s3" to anyList); println(map); println()
    }

    @Test
    fun test2() {
        println(int99)
    }

    @Test
    fun test3() {

    }

}