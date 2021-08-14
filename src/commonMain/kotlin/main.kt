import com.soywiz.korge.Korge
import com.soywiz.korge.service.storage.storage
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.ScaleMode
import gui.Icons
import gui.startSplash
import gui.sudokuGui
import gui.theme
import storage.Storage
import sudoku.Sudoku
import sudoku.Sudoku.Companion.dummySudoku
import sudoku.Sudoku.Companion.loadSudokuFromStorage


var game = dummySudoku()


suspend fun main() = Korge(
	scaleAnchor = Anchor.TOP_LEFT,
	scaleMode = ScaleMode.NO_SCALE,
	clipBorders = false,
	bgcolor = theme.background
) {
	if (views.nativeWidth == views.virtualWidth && views.nativeHeight == views.virtualHeight) {
		views.scaleAnchor = Anchor.MIDDLE_CENTER
		views.scaleMode = ScaleMode.SHOW_ALL
		views.clipBorders = true
	}

	Storage.nativeStorage = storage
	Icons.load()

	try {
		game = loadSudokuFromStorage()
	} catch (e: Exception) {
		println("${Sudoku.storageKey} loading from Storage failed")
	}

	if (game.board.size == 0)
		startSplash(
			views.nativeWidth.toDouble(),
			views.nativeHeight.toDouble()
		)
	else
		sudokuGui(
			views.nativeWidth.toDouble(),
			views.nativeHeight.toDouble(),
			loadFromStorage = true
		)
}