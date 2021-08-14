package sudoku

import kotlin.properties.Delegates

class Validator(private val sudoku: Sudoku) {
    lateinit var failedIndicesCallback: (Set<Int>, Set<Int>) -> Unit
    lateinit var solvedCallback: () -> Unit

    lateinit var completedValuesCallback: (List<Int>, List<Int>) -> Unit

    private var failedIndices by Delegates.observable(setOf<Int>()) { _, oldValue, newValue ->
        if (::failedIndicesCallback.isInitialized) failedIndicesCallback(
            oldValue,
            newValue
        )
    }
    private var completedValues by Delegates.observable(
        listOf<Int>()
    ) { _, oldValue, newValue ->
        if (::completedValuesCallback.isInitialized) completedValuesCallback(
            oldValue,
            newValue
        )
    }
    fun isSolved() = failedIndices.isEmpty() && sudoku.board.data.all { it != 0 }
    fun isValid() = failedIndices.isEmpty()

    fun reinitialize() {
        validate()
        calcCompletedValues()

        if (::solvedCallback.isInitialized && isSolved())
            solvedCallback()
    }

    private fun validate() {
        val failedIndices = mutableSetOf<Int>()

        for (partIndices in sudoku.board.indicesByParts) {
            val valueCount = Array(sudoku.board.blockSize + 1) { mutableListOf<Int>() }
            for (index in partIndices) {
                valueCount[sudoku.board.data[index]].add(index)
            }
            for (i in sudoku.board.values)
                if (valueCount[i].size > 1) failedIndices.addAll(valueCount[i])
        }
        this.failedIndices = failedIndices
    }

    fun calcCompletedValues() {
        val completedValues = mutableListOf<Int>()
        val valueFrequency = IntArray(sudoku.board.blockSize + 1)

        for (i in 0 until sudoku.board.size) valueFrequency[sudoku.board.data[i]]++

        val failedValues = failedIndices.toList().map { sudoku.board.data[it] }
        for ((value, count) in valueFrequency.withIndex()) {
            if (count == sudoku.board.blockSize && value !in failedValues)
                completedValues.add(value)
        }

        this.completedValues = completedValues
    }
}