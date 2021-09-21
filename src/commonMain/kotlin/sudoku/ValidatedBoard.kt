package sudoku

import kotlin.properties.Delegates.observable


interface ValidatedBoard: Board {
    val failedIndices: Set<Int>
    val completedValues: List<Int>

    var failedIndicesCallback: (Set<Int>, Set<Int>) -> Unit
    var solvedCallback: () -> Unit
    var completedValuesCallback: (List<Int>, List<Int>) -> Unit

    fun isSolved() = failedIndices.isEmpty() && data.all { it != 0 }
    fun isValid() = failedIndices.isEmpty()
    fun reinitialize()
}


class ValidatedBoardImpl(
    blockSizeX: Int,
    blockSizeY: Int,
    data: IntArray
): ValidatedBoard, BoardImpl(blockSizeX, blockSizeY, data) {
    override lateinit var failedIndicesCallback: (Set<Int>, Set<Int>) -> Unit
    override lateinit var solvedCallback: () -> Unit
    override lateinit var completedValuesCallback: (List<Int>, List<Int>) -> Unit

    override var failedIndices by observable(setOf<Int>()) { _, oldValue, newValue ->
        if (::failedIndicesCallback.isInitialized) failedIndicesCallback(
            oldValue,
            newValue
        )
    }
        private set

    override var completedValues by observable(
        listOf<Int>()
    ) { _, oldValue, newValue ->
        if (::completedValuesCallback.isInitialized) completedValuesCallback(
            oldValue,
            newValue
        )
    }
        private set

    override fun reinitialize() {
        validate()
        calcCompletedValues()
        if (::solvedCallback.isInitialized && isSolved())
            solvedCallback()
    }

    private fun validate() {
        val failedIndices = mutableSetOf<Int>()

        for (partIndices in indicesByParts) {
            val valueCount = Array(blockSize + 1) { mutableListOf<Int>() }
            for (index in partIndices) {
                valueCount[data[index]].add(index)
            }
            for (i in values)
                if (valueCount[i].size > 1) failedIndices.addAll(valueCount[i])
        }
        this.failedIndices = failedIndices
    }

    private fun calcCompletedValues() {
        val completedValues = mutableListOf<Int>()
        val valueFrequency = IntArray(blockSize + 1)

        for (i in 0 until size) valueFrequency[data[i]]++

        val failedValues = failedIndices.toList().map { data[it] }
        for ((value, count) in valueFrequency.withIndex()) {
            if (count == blockSize && value !in failedValues)
                completedValues.add(value)
        }

        this.completedValues = completedValues
    }
}