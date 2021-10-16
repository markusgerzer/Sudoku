package sudoku

import com.soywiz.korio.serialization.json.Json
import storage.Storage


class Sudoku constructor(
    private val validatedBoard: ValidatedBoard,
    notesData: List<List<Int>> = List(validatedBoard.size) { listOf<Int>() }
) : Storage.SelfStorable, ValidatedBoard by validatedBoard {

    val immutableIndices = mutableSetOf<Int>()
    var solvedBoard: List<Int>? = null
        internal set

    val candidates:Candidates = Candidates(validatedBoard)
    val notes = Notes(validatedBoard, notesData).also {
        it.changedCallback.add { _, _ ->
            saveToStorage()
        }
    }

    operator fun get(index: Int) = boardArray[index]

    operator fun set(index: Int, value: Int) {
        if (value !in values && value != 0) throw IllegalArgumentException("Value not valid!")
        if (index in immutableIndices) return
        boardArray[index] = value
        reinitialize()
        if (isValid()) notes.adjust(index, value)
        else saveToStorage()
    }

    override fun reinitialize() {
        validatedBoard.reinitialize()
        candidates.reCalc()
    }

    fun reset() {
        for (i in 0 until size)
            if (i !in immutableIndices)
                boardArray[i] = 0
        candidates.reCalc()
        notes.clear()
    }

    override val storageKey: String get() = Sudoku.storageKey
    private fun toMap() = mapOf(
        "blockSizeX" to blockSizeX,
        "blockSizeY" to blockSizeY,
        "boardArray" to boardArray.toList(),
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
            Sudoku(ValidatedBoardImpl(0, 0, IntArray(0)))

        fun blankSudoku(blockSizeX: Int, blockSizeY: Int) =
            Sudoku(
                ValidatedBoardImpl(
                    blockSizeX,
                    blockSizeY,
                    IntArray(blockSizeX * blockSizeY * blockSizeX * blockSizeY)
                )
            ).apply { saveToStorage() }

        fun loadSudokuFromStorage(): Sudoku {
            val map = Storage.loadFromStorage(storageKey) as Map<*, *>
            val blockSizeX = map["blockSizeX"] as Int
            val blockSizeY = map["blockSizeY"] as Int
            val boardArray = (map["boardArray"] as List<Int>).toIntArray()
            val board = ValidatedBoardImpl(blockSizeX, blockSizeY, boardArray)
            val solvedBoard = map["solvedBoard"] as List<Int>?
            val immutableIndices = map["immutableIndices"] as List<Int>
            val notesData = map["notes"] as List<List<Int>>

            return Sudoku(board, notesData = notesData).apply{
                this.solvedBoard = solvedBoard
                this.immutableIndices.addAll(immutableIndices)
            }
        }

        fun createSudoku(blockSizeX: Int, blockSizeY: Int) = createSudoku2(blockSizeX, blockSizeY)

        fun createSudoku1(blockSizeX: Int, blockSizeY: Int): Sudoku {
            val sudoku = blankSudoku(blockSizeX, blockSizeY)
            val solver = Solver(sudoku)
            while (!solver.uniqueSolution.solve()) {
                println("----------------")
                val emptyIndices = (0 until sudoku.size).filter { sudoku[it] == 0 }
                val index = emptyIndices.random()
                val value = sudoku.candidates.getAt(index).random()
                solver.internSet(index, value)

                if(solver.firstSolution.solve()) {
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
                !sudoku.isValid() ||
                !solver.firstSolution.solve() ||
                !sudoku.immutableIndices.addAll(randomIndices)
            )
            sudoku.reset()


            while (!solver.uniqueSolution.solve()) {
                println("----------------")
                val emptyIndices = (0 until sudoku.size).filter { sudoku[it] == 0 }
                val index = emptyIndices.random()
                val value = sudoku.candidates.getAt(index).random()
                solver.internSet(index, value)

                if(solver.firstSolution.solve()) {
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

        fun createSudoku3(blockSizeX: Int, blockSizeY: Int): Sudoku {
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
                !sudoku.isValid() ||
                !solver.firstSolution.solve() ||
                !sudoku.immutableIndices.addAll(randomIndices)
            )
            sudoku.reset()

            solver.allSolution.solve()
            val tree =
                solver.allSolution.solutionTree
            for (node in tree.traversLevelOrder()) {
                for ((i, nextNode) in node.nextNodes.withIndex()) {
                    if (nextNode != null && nextNode.index == -1) {
                        val boardArray =
                            node.boardArray.copyOf()
                        boardArray[node.index] =
                            node.values[i]
                        val board =
                            ValidatedBoardImpl(
                                blockSizeX,
                                blockSizeY,
                                boardArray
                            )
                        return Sudoku(board)
                    }
                }
            }

            throw IllegalStateException()
        }
    }
}