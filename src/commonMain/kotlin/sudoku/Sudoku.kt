package sudoku

import com.soywiz.korio.serialization.json.Json
import storage.Storage


class Sudoku constructor(
    board: Board,
    notesData: List<List<Int>> = List(board.data.size) { listOf<Int>() }
) : Storage.SelfStorable, Board by board {

    val immutableIndices = mutableSetOf<Int>()
    var solvedBoard: List<Int>? = null
        internal set

    val candidates = Candidates(this)
    val validator = Validator(this)
    val notes = Notes(this, notesData)

    operator fun get(index: Int) = data[index]

    operator fun set(index: Int, value: Int) {
        if (index in immutableIndices) return
        if (value !in values && value != 0) throw IllegalArgumentException("Value not valid!")
        data[index] = value
        reinitialize()
        if (validator.isValid()) notes.adjust(index, value)
        saveToStorage()
    }

    fun reinitialize() {
        validator.reinitialize()
        candidates.reCalc()
    }

    fun reset() {
        for (i in 0 until size)
            if (i !in immutableIndices)
                data[i] = 0
        candidates.reCalc()
        notes.clear()
    }

    override val storageKey: String get() = Sudoku.storageKey
    private fun toMap() = mapOf(
        "blockSizeX" to blockSizeX,
        "blockSizeY" to blockSizeY,
        "data" to data.toList(),
        "solvedBoard" to solvedBoard,
        "immutableIndices" to immutableIndices,
        "notes" to notes
    )
    override fun encodeToJson(b: StringBuilder) {
        b.append(Json.stringify(toMap()))
    }

    override fun toString() = toMap().toString()


    companion object {
        const val storageKey = "Sudoku"

        fun dummySudoku() =
            Sudoku(BoardImpl(0, 0, IntArray(0)))

        fun blankSudoku(blockSizeX: Int, blockSizeY: Int) =
            Sudoku(
                BoardImpl(
                    blockSizeX,
                    blockSizeY,
                    IntArray(blockSizeX * blockSizeY * blockSizeX * blockSizeY)
                )
            ).apply { saveToStorage() }

        fun loadSudokuFromStorage(): Sudoku {
            val map = Storage.loadFromStorage(storageKey) as Map<*, *>
            val blockSizeX = map["blockSizeX"] as Int
            val blockSizeY = map["blockSizeY"] as Int
            val boardData = (map["data"] as List<Int>).toIntArray()
            val board = BoardImpl(blockSizeX, blockSizeY, boardData)
            val solvedBoard = map["solvedBoard"] as List<Int>?
            val immutableIndices = map["immutableIndices"] as List<Int>
            val notesData = map["notes"] as List<List<Int>>

            return Sudoku(board, notesData).apply{
                this.solvedBoard = solvedBoard
                this.immutableIndices.addAll(immutableIndices)
            }
        }

        fun createSudoku(blockSizeX: Int, blockSizeY: Int) = createSudoku2(blockSizeX, blockSizeY)

        fun createSudoku1(blockSizeX: Int, blockSizeY: Int): Sudoku {
            val sudoku = blankSudoku(blockSizeX, blockSizeY)
            val solver = Solver(sudoku)
            while (!solver.solve(true)) {
                println("----------------")
                val emptyIndices = (0 until sudoku.size).filter { sudoku[it] == 0 }
                val index = emptyIndices.random()
                val value = sudoku.candidates.getAt(index).random()
                solver.internSet(index, value)

                if(solver.solve(false)) {
                    sudoku.immutableIndices.add(index)
                } else {
                    solver.internSet(index, 0)
                }

                sudoku.reset()
            }

            sudoku.reset()
            sudoku.saveToStorage()
            return sudoku
        }

        fun createSudoku2(blockSizeX: Int, blockSizeY: Int): Sudoku {
            val sudoku = blankSudoku(blockSizeX, blockSizeY)
            val solver = Solver(sudoku)

            do {
                println("++++++++++++++++")
                val randomIndices = (0 until sudoku.size).shuffled().take(sudoku.blockSize)
                repeat(sudoku.blockSize) { i ->
                    val index = randomIndices[i]
                    val value = sudoku.candidates.getAt(index).random()
                    solver.internSet(index, value)
                }
            } while (
                !sudoku.validator.isValid() &&
                !solver.solve(false) &&
                sudoku.immutableIndices.addAll(randomIndices)
            )
            sudoku.reset()


            while (!solver.solve(true)) {
                println("----------------")
                val emptyIndices = (0 until sudoku.size).filter { sudoku[it] == 0 }
                val index = emptyIndices.random()
                val value = sudoku.candidates.getAt(index).random()
                solver.internSet(index, value)

                if(solver.solve(false)) {
                    sudoku.immutableIndices.add(index)
                } else {
                    solver.internSet(index, 0)
                }

                sudoku.reset()
            }

            sudoku.reset()
            sudoku.saveToStorage()
            return sudoku
        }

        /*
        fun createSudoku2(blockSizeX: Int, blockSizeY: Int): Sudoku {
            fun setFirstValues(blockSizeX: Int, blockSizeY: Int): Sudoku {
                var sudoku: Sudoku
                do {
                    sudoku = blankSudoku(blockSizeX, blockSizeY)

                    // Set 27% - 44% of the Sudoku values at start.
                    val range = (.27 * sudoku.size).roundToInt()..(.44 * sudoku.size).roundToInt()
                    val nStartValues = range.random()

                    for (i in 0 until nStartValues) {
                        var index: Int
                        do {
                            // Set gameIndex by random
                            index = (0 until sudoku.size).random()
                            // Since startIndices is as Set, we can test the return value of
                            // the add method to make sure the gameIndex is unique.
                        } while (!sudoku._immutableIndices.add(index))
                    }

                    // Set each value 1 time at the first n startIndices. n = size
                    for ((i, value) in sudoku.values.withIndex()) {
                        val index = sudoku._immutableIndices.elementAt(i)
                        sudoku[index] = value
                    }

                } while (!sudoku.solve(false))
                sudoku.reset()

                return sudoku
            }

            var sudoku: Sudoku
            do {
                sudoku = setFirstValues(blockSizeX, blockSizeY)
            } while (!sudoku.solve(true))

            sudoku.reset()
            return sudoku
        }
         */
    }
}