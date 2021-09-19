package sudoku


interface Board {
    val blockSizeX: Int
    val blockSizeY: Int
    val data: IntArray
    val blockSize: Int
    val size get() = data.size
    val values: IntRange
    val partsAtIndex: List<List<Int>>
    val indicesByRows: List<List<Int>>
    val indicesByCols: List<List<Int>>
    val indicesByBlocks: List<List<Int>>
    val indicesByParts: List<List<Int>>
    val indexAffects: List<Set<Int>>
    fun freeIndices() = (0 until size).filter { data[it] == 0 }
}


open class BoardImpl(
    override val blockSizeX: Int,
    override val blockSizeY: Int,
    override val data: IntArray
    ) : Board {

    override val blockSize = blockSizeX * blockSizeY
    override val values = 1..blockSize

    init {
        if (blockSize * blockSize != size)
            throw IllegalArgumentException("Size Error")
        for (field in data)
            if (field != 0 && field !in values)
                throw IllegalArgumentException("Illegal values in game!")
    }

    override val partsAtIndex = List(size) { i ->
        val row = i / blockSize
        val col = i % blockSize
        val block = (col / blockSizeX) + blockSizeY * (row / blockSizeY)
        listOf(row, col + blockSize, block + 2 * blockSize)
    }

    override val indicesByRows = List(blockSize) { row ->
        List(blockSize) { col -> row * blockSize + col }
    }
    override val indicesByCols = List(blockSize) { col ->
        List(blockSize) { row -> row * blockSize + col }
    }
    override val indicesByBlocks = List(blockSize) { block ->
        List(blockSize) { blockIndex ->
            val blockRowN   = block / blockSizeY
            val rowInBlockN = blockIndex / blockSizeX
            val rowN        = blockRowN * blockSizeY + rowInBlockN
            val blockColN   = block % blockSizeY
            val colInBlockN = blockIndex % blockSizeX
            val colN        = blockColN * blockSizeX + colInBlockN
            rowN * blockSize + colN
        }
    }
    override val indicesByParts = indicesByRows + indicesByCols + indicesByBlocks

    override val indexAffects = List(size) { i ->
        val set = mutableSetOf<Int>()
        for (part in partsAtIndex[i])
            set.addAll(indicesByParts[part])
        set.toSet()
    }
}