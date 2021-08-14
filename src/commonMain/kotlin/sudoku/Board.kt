package sudoku

import com.soywiz.korio.serialization.json.Json

class Board(
    val blockSizeX: Int,
    val blockSizeY: Int,
    val data: IntArray
    ) : Json.CustomSerializer {

    val blockSize = blockSizeX * blockSizeY
    val size = data.size

    val values = 1..blockSize

    init {
        if (blockSize * blockSize != size)
            throw IllegalArgumentException("Size Error")
        for (field in data)
            if (field != 0 && field !in values)
                throw IllegalArgumentException("Illegal values in game!")
    }

    val partsAtIndex = List(size) { i ->
        val row = i / blockSize
        val col = i % blockSize
        val block = (col / blockSizeX) + blockSizeY * (row / blockSizeY)
        listOf(row, col + blockSize, block + 2 * blockSize)
    }

    val indicesByRows = List(blockSize) { row ->
        List(blockSize) { col -> row * blockSize + col }
    }
    val indicesByCols = List(blockSize) { col ->
        List(blockSize) { row -> row * blockSize + col }
    }
    val indicesByBlocks = List(blockSize) { block ->
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
    val indicesByParts = indicesByRows + indicesByCols + indicesByBlocks

    val indexAffects = List(size) { i ->
        val set = mutableSetOf<Int>()
        for (part in partsAtIndex[i])
            set.addAll(indicesByParts[part])
        set.toSet()
    }

    fun freeIndices() = (0 until size).filter { data[it] == 0 }

    private fun toMap() = mapOf(
        "blockSizeX" to blockSizeX,
        "blockSizeY" to blockSizeY,
        "data" to data.toList()
    )
    override fun encodeToJson(b: StringBuilder) {
        Json.stringify(toMap(), b)
    }
}