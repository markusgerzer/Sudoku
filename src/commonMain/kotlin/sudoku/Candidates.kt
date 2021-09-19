package sudoku

import kotlin.properties.Delegates

class Candidates(private val board: Board) {
    lateinit var changedCallback: (Array<List<Int>>, Array<List<Int>>) -> Unit
    private var data by Delegates.observable(
        Array(
            board.size
        ) { listOf<Int>() }) { _, oldValue, newValue ->
        if (::changedCallback.isInitialized) changedCallback(
            oldValue,
            newValue
        )
    }
    init { reCalc() }

    fun getAt(index: Int) = data[index]

    fun inPart(n: Int) = board.indicesByParts[n]
        .map { data[it] }
        .flatten()

    fun reCalc() {
        data = Array(board.size) {
            if (board.data[it] == 0) board.values - noCandidatesAt(it)
            else listOf()
        }
    }

    fun adjust(index: Int, value: Int) {
        data[index] = emptyList()
        for (i in board.indexAffects[index])
            data[i] = data[i] - value
    }

    private fun valuesInPart(n: Int) = board.indicesByParts[n]
        .map { board.data[it] }
        .filter { it != 0 }

    private fun noCandidatesAt(index: Int): List<Int> {
        val (row, col, block) = board.partsAtIndex[index]
        return (valuesInPart(row) + valuesInPart(col) + valuesInPart(block))
            .toSet()
            .toList()
    }
}