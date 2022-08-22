import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.tests.KorgeTest
import com.soywiz.korio.async.runBlockingNoJs
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
        //val timeCreateSudoku1 = measureTime {
        //    repeat(5) { Sudoku.createSudoku1(3, 3) }
        //}
        //println("***************************************")
        val timeCreateSudoku2 = measureTime {
            repeat(10) { runBlockingNoJs { Sudoku.createSudoku2(3, 3) } }
        }
        //println("***************************************")
        //val timeCreateSudoku3 = measureTime {
        //    repeat(1) { Sudoku.createSudoku3(3, 3) }
        //}
        println("***************************************")
        //println("createSudoku1: $timeCreateSudoku1")
        println("createSudoku2: $timeCreateSudoku2")
        //println("createSudoku3: $timeCreateSudoku3")
        //println(Sudoku.createSudoku3(3, 3))
    }

    @Test
    fun createMultiTest() {
        runBlockingNoJs {
            Sudoku.createSudokuWithMultipleTreads(3, 3)
        }
    }
}