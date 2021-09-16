package sudoku

import kotlin.properties.Delegates

class Candidates(private val sudoku: Sudoku) {
    lateinit var changedCallback: (Array<List<Int>>, Array<List<Int>>) -> Unit
    private var data by Delegates.observable(
        Array(
            sudoku.size
        ) { listOf<Int>() }) { _, oldValue, newValue ->
        if (::changedCallback.isInitialized) changedCallback(
            oldValue,
            newValue
        )
    }
    init { reCalc() }

    fun getAt(index: Int) = data[index]

    fun inPart(n: Int) = sudoku.indicesByParts[n]
        .map { data[it] }
        .flatten()

    fun reCalc() {
        data = Array(sudoku.size) {
            if (sudoku.data[it] == 0) sudoku.values - noCandidatesAt(it)
            else listOf()
        }
    }

    fun adjust(index: Int, value: Int) {
        data[index] = emptyList()
        for (i in sudoku.indexAffects[index])
            data[i] = data[i] - value
    }

    private fun valuesInPart(n: Int) = sudoku.indicesByParts[n]
        .map { sudoku.data[it] }
        .filter { it != 0 }

    private fun noCandidatesAt(index: Int): List<Int> {
        val (row, col, block) = sudoku.partsAtIndex[index]
        return (valuesInPart(row) + valuesInPart(col) + valuesInPart(block))
            .toSet()
            .toList()
    }
}