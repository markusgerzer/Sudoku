package sudoku

import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korio.async.withTimeout
import kotlinx.coroutines.withTimeoutOrNull


interface SolverI {
    fun solve(): Boolean
    suspend fun solveTimeout(): Boolean
}


class Solver (private val sudoku: Sudoku): SolverI {

    internal fun internSet(index: Int, value: Int) {
        sudoku.boardArray[index] = value
        sudoku.candidates.adjust(index, value)
    }

    override fun solve() = solve(::defaultSolveBacktrack, ::defaultSolveImpl)
    override suspend fun solveTimeout() = solveTimeout(::defaultSolveBacktrack, ::defaultSolveImpl)

    private suspend fun solveTimeout(
        solveBacktrack: () -> Boolean,
        solveImpl: (()->Boolean) -> Boolean = ::defaultSolveImpl
    ) = withTimeoutOrNull(1) {
        solveImpl(solveBacktrack)
            .also { success ->
                if (success) {
                    sudoku.solvedBoard =
                        sudoku.boardArray.toList()
                    println("T")
                } else
                    println("N")
            }
    } ?: println("!!!Timeout!!!").run { false }

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
     * returns true if the first solution is found.
     */
    val firstSolution = object : SolverI {
        override fun solve(): Boolean = solve(::firstSolutionBacktrack)
        override suspend fun solveTimeout(): Boolean = solveTimeout(::firstSolutionBacktrack)
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
        override suspend fun solveTimeout(): Boolean = solveTimeout(::uniqueSolutionBacktrack, ::uniqueSolutionSolveImpl)

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

    /**
     *
     */
    interface AllSolutionSolver: SolverI {
        val solutionTree: SolutionTree
        var fast: Boolean
    }
    val allSolution = object : AllSolutionSolver {
        override val solutionTree = SolutionTree()
        override fun solve() = solve(::allSolutionBacktrack, ::allSolutionSolveImpl)
        override suspend fun solveTimeout() = solveTimeout(::allSolutionBacktrack, ::allSolutionSolveImpl)
        override var fast = true

        private fun allSolutionSolveImpl (allSolutionBacktrack: ()->Boolean) : Boolean {
            val savedBoard = sudoku.boardArray.copyOf()
            solutionTree.clear()
            solveLoop2(allSolutionBacktrack)
            savedBoard.forEachIndexed { index, value ->
                sudoku.boardArray[index] = value
            }
            sudoku.candidates.reCalc()

            return solutionTree.solutions > 0
        }

        private tailrec fun solveLoop2(solveBacktrack: ()->Boolean): Boolean = when {
            sudoku.isSolved() -> {
                print("t")
                val leafNode =
                    if (sudoku.isSolved())
                        SolutionTree.Node(
                            sudoku.boardArray.copyOf(),
                            -1,
                            listOf<Int>()
                        )
                    else null
                solutionTree.addLeaf(leafNode)
                true }
            solve1() -> { solveLoop2(solveBacktrack) }
            solve2() -> { solveLoop2(solveBacktrack) }
            solveBacktrack() -> { true }
            else -> { print("n"); false }
        }

        private fun allSolutionBacktrack(): Boolean {
            print("B")
            val backtrackIndex = sudoku.candidates.indexWithMinCandidates()
            val node = SolutionTree.Node(
                sudoku.boardArray.copyOf(),
                backtrackIndex,
                sudoku.candidates.getAt(backtrackIndex)
            )
            solutionTree.addNode(node)

            val savedBoard = sudoku.boardArray.copyOf()
            var shortcut = node.depth >= solutionTree.depth - 1 && solutionTree.solutions > 0
            for (value in sudoku.candidates.getAt(backtrackIndex)) {
                if (fast and shortcut) solutionTree.addLeaf(null)
                else {
                    internSet(backtrackIndex, value)
                    shortcut = solveLoop2(::allSolutionBacktrack)
                    for (i in sudoku.boardArray.indices)
                        sudoku.boardArray[i] = savedBoard[i]
                    sudoku.candidates.reCalc()
                }
            }
            return false
        }
    }
}