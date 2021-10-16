import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.tests.KorgeTest
import storage.Storage
import sudoku.Sudoku
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class CreateSudokuTest : KorgeTest() {
    init {
        Storage.nativeStorage = stage.storage
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun create3x3test1() {
        val time = measureTime {
            repeat(10) { Sudoku.createSudoku1(3, 3) }
        }
        println("***************************************")
        println("createSudoku1: $time")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun create3x3test2() {
        val time = measureTime {
            repeat(10) { Sudoku.createSudoku2(3, 3) }
        }
        println("***************************************")
        println("createSudoku1: $time")
    }
}