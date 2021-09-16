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
    fun create3x3test() {
        val timeCreateSudoku1 = measureTime {
            repeat(1) { Sudoku.createSudoku1(3, 3) }
        }
        val timeCreateSudoku2 = measureTime {
            repeat(1) { Sudoku.createSudoku2(3, 3) }
        }
        println("createSudoku1: $timeCreateSudoku1")
        println("createSudoku2: $timeCreateSudoku2")

    }
}