package sudoku

import com.soywiz.korio.serialization.json.Json
import kotlin.properties.Delegates

class Notes(
    private val board: Board,
    notesData: List<List<Int>> = List(board.size) { listOf<Int>() }
): Json.CustomSerializer {
    val changedCallback = mutableListOf<(List<List<Int>>, List<List<Int>>) -> Unit>()
    private var data by Delegates.observable(notesData) { _, oldValue, newValue ->
        for (callback in changedCallback)
            callback(oldValue, newValue)
    }

    fun clear() {
        data = List(board.size) { listOf<Int>() }
    }
    fun getAt(index: Int) = data[index]
    fun addAt(index: Int, value: Int) {
        data = data.mapIndexed { index1: Int, list: List<Int> ->
            if (index == index1) list + value
            else list
        }
    }
    fun delAt(index: Int, value: Int) {
        data = data.mapIndexed { index1: Int, list: List<Int> ->
            if (index == index1) list - value
            else list
        }
    }
    fun adjust(index: Int, value: Int) {
        data = data.mapIndexed { index1: Int, list: List<Int> ->
            if (index1 in board.indexAffects[index]) {
                list - value
            } else list
        }
    }

    override fun encodeToJson(b: StringBuilder) {
        Json.stringify(data, b)
    }
}