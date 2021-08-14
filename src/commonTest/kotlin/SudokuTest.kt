/*
import com.soywiz.korio.serialization.json.Json
import sudoku.createSudoku
import sudoku.loadSudokuFromJson
import kotlin.test.Test
import kotlin.test.assertEquals

class SudokuTest {
    private val sudoku = createSudoku(3, 3)

    @Test
    fun encodeToJsonTest() {
        val json = Json.stringify(sudoku)
        val sudoku1 = loadSudokuFromJson(json)
        assertEquals(sudoku.blockSizeX, sudoku1.blockSizeY)
        assertEquals(sudoku.blockSizeY, sudoku1.blockSizeY)
        assertEquals(sudoku.blockSize, sudoku1.blockSize)
        assertEquals(sudoku.board, sudoku1.board)
        assertEquals(sudoku._immutableIndices, sudoku1._immutableIndices)
        assertEquals(sudoku.size, sudoku1.size)
        assertEquals(sudoku.values, sudoku1.values)
        assertEquals(sudoku.solvedBoard, sudoku1.solvedBoard)
        assertEquals(sudoku.candidates.toList(), sudoku1.candidates.toList())
    }
}

 */