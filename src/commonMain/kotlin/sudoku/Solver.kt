package sudoku

class Solver(private val sudoku: Sudoku) {

    class MultiSolution : Throwable()

    private var savedBoard: IntArray? = null

    internal fun internSet(index: Int, value: Int) {
        sudoku.board.data[index] = value
        sudoku.candidates.adjust(index, value)
    }

    fun solve(oneSolution: Boolean = true): Boolean {
        savedBoard = null
        return (if (try {
                solveLoop()
            } catch (e: MultiSolution) {
                if (oneSolution) {
                    savedBoard?.forEachIndexed { index, value ->
                        sudoku.board.data[index] = value
                    }
                    sudoku.candidates.reCalc()
                    false
                } else true
            }) {
            sudoku.solvedBoard = sudoku.board.data.toList()
            true
        } else false).also {
            println(if (it) "T" else "N")
        }
    }

    private tailrec fun solveLoop(): Boolean = when {
        sudoku.validator.isSolved() -> { print("t"); true }
        solve1() -> { solveLoop() }
        solve2() -> { solveLoop() }
        solveBacktrack() -> { solveLoop() }
        else -> { print("n"); false }
    }

    /**
     * Count candidates
     */
    private fun solve1(): Boolean {
        print(1)
        var valuesSolved = 0
        for (i in 0 until sudoku.board.size) {
            if (sudoku.candidates.getAt(i).size == 1) {
                internSet(i, sudoku.candidates.getAt(i)[0])
                ++valuesSolved
            }
        }
        print(".".repeat(valuesSolved))
        return valuesSolved > 0
    }

    /**
     * Count candidates pro part
     */
    private fun solve2(): Boolean {
        print(2)
        var valuesSolved = 0
        for (i in 0 until sudoku.board.size) {
            val candidatesInCurrentParts = sudoku.board.partsAtIndex[i].map { sudoku.candidates.inPart(it) }
            for (value in sudoku.candidates.getAt(i)) {
                for (candidatesInCurrentPart in candidatesInCurrentParts) {
                    if (candidatesInCurrentPart.count { it == value } == 1) {
                        internSet(i, value)
                        ++valuesSolved
                    }
                }
            }
        }
        print(".".repeat(valuesSolved))
        return valuesSolved > 0
    }

    private fun solveBacktrack(): Boolean {
        print("B")
        if (savedBoard == null) savedBoard = sudoku.board.data.copyOf()

        var result: IntArray? = null
        var lowestCandidatesSize = sudoku.board.blockSize
        var backtrackIndex = 0

        for (i in 0 until sudoku.board.size) {
            if (sudoku.board.data[i] == 0 && sudoku.candidates.getAt(i).size < lowestCandidatesSize) {
                backtrackIndex = i
                lowestCandidatesSize = sudoku.candidates.getAt(i).size
            }
        }

        val currentSavedBoard = sudoku.board.data.copyOf()
        for (value in sudoku.candidates.getAt(backtrackIndex)) {
            internSet(backtrackIndex, value)
            if (solveLoop()) {
                if (result == null) result = sudoku.board.data.copyOf()
                else throw MultiSolution()
            }
            for (i in sudoku.board.data.indices) sudoku.board.data[i] = currentSavedBoard[i]
            sudoku.candidates.reCalc()
        }

        return if (result == null) false
        else {
            for (i in sudoku.board.data.indices) { sudoku.board.data[i] = result[i] }
            true
        }
    }
}