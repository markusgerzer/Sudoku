package sudoku


interface SolverI {
    fun solve(): Boolean
}


class Solver (private val sudoku: Sudoku): SolverI {

    internal fun internSet(index: Int, value: Int) {
        sudoku.boardArray[index] = value
        sudoku.candidates.adjust(index, value)
    }

    override fun solve() = solve(::defaultSolveBacktrack, ::defaultSolveImpl)

    private fun solve(
        solveBacktrack: () -> Boolean,
        solveImpl: (()->Boolean) -> Boolean = ::defaultSolveImpl
    ) = solveImpl(solveBacktrack)
        .also { success ->
            if (success) {
                sudoku.solvedBoard =
                    sudoku.boardArray.toList()
                println("T")
            } else
                println("N")
        }

    private fun defaultSolveImpl(solveBacktrack: () -> Boolean) =
        solveLoop(solveBacktrack)

    private tailrec fun solveLoop(solveBacktrack: ()->Boolean): Boolean = when {
        sudoku.isSolved() -> { print("t"); true }
        solve1() -> { solveLoop(solveBacktrack) }
        solve2() -> { solveLoop(solveBacktrack) }
        solveBacktrack() -> { true }
        else -> { print("n"); false }
    }

    private fun defaultSolveBacktrack() = false

    /**
     * Count candidates
     */
    private fun solve1(): Boolean {
        print(1)
        var valuesSolved = 0
        for (i in 0 until sudoku.size) {
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
        for (i in 0 until sudoku.size) {
            val candidatesInCurrentParts =
                sudoku.partsAtIndex[i].map {
                    sudoku.candidates.inPart(it)
                }
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

    /**
     * Find one Solution
     * Methode solve() doesn't test for more solutions
     * and true if the first solution is found.
     */
    val firstSolution = object : SolverI {
        override fun solve(): Boolean = solve(::firstSolutionBacktrack)
        private fun firstSolutionBacktrack(): Boolean {
            print("B")

            val backtrackIndex = sudoku.candidates.indexWithMinCandidates()
            val savedBoard = sudoku.boardArray.copyOf()

            for (value in sudoku.candidates.getAt(backtrackIndex)) {
                internSet(backtrackIndex, value)
                if (solveLoop(::firstSolutionBacktrack)) return true
                for (i in sudoku.boardArray.indices) sudoku.boardArray[i] = savedBoard[i]
                sudoku.candidates.reCalc()
            }
            return false
        }
    }

    /**
     * Find unique Solution
     * Methode solve() tests for more solutions and
     * returns true only if exact one solution is found.
     */
    val uniqueSolution = object : SolverI{
        inner class MultiSolution : Throwable()
        private var savedBoard: IntArray? = null
        override fun solve(): Boolean = solve(::uniqueSolutionBacktrack, ::uniqueSolutionSolveImpl)

        private fun uniqueSolutionSolveImpl(uniqueSolutionBacktrack: ()->Boolean) : Boolean {
            savedBoard = null
            return try {
                solveLoop(uniqueSolutionBacktrack)
            } catch (e: MultiSolution) {
                savedBoard?.forEachIndexed { index, value ->
                    sudoku.boardArray[index] = value
                }
                sudoku.candidates.reCalc()
                false
            }
        }

        private fun uniqueSolutionBacktrack(): Boolean {
            print("B")
            if (savedBoard == null) savedBoard = sudoku.boardArray.copyOf()

            var result: IntArray? = null
            val backtrackIndex = sudoku.candidates.indexWithMinCandidates()
            val currentSavedBoard = sudoku.boardArray.copyOf()

            for (value in sudoku.candidates.getAt(backtrackIndex)) {
                internSet(backtrackIndex, value)
                if (solveLoop(::uniqueSolutionBacktrack)) {
                    if (result == null) result = sudoku.boardArray.copyOf()
                    else throw MultiSolution()
                }
                for (i in sudoku.boardArray.indices) sudoku.boardArray[i] = currentSavedBoard[i]
                sudoku.candidates.reCalc()
            }

            return if (result == null) false
            else {
                for (i in sudoku.boardArray.indices) { sudoku.boardArray[i] = result[i] }
                true
            }
        }
    }
}