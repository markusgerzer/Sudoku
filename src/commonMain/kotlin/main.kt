import com.soywiz.korev.ReshapeEvent
import com.soywiz.korev.addEventListener
import com.soywiz.korge.Korge
import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.view.Container
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


suspend fun main(): Unit = Korge(
	scaleAnchor = Anchor.TOP_LEFT,
	scaleMode = ScaleMode.SHOW_ALL,
	clipBorders = false,
	bgcolor = theme.background
) {
	Storage.nativeStorage = storage
	Icons.load()

	addEventListener<ReshapeEvent> {
		removeChildren()
		startGame(
			views.actualVirtualWidth.toDouble(),
			views.actualVirtualHeight.toDouble(),
		)
	}

	startGame(
		views.actualVirtualWidth.toDouble(),
		views.actualVirtualHeight.toDouble(),
	)
}


fun Container.startGame(
	width: Double,
	height: Double
) {
	try {
		game = loadSudokuFromStorage()
	} catch (e: Exception) {
		println("${Sudoku.storageKey} loading from Storage failed")
	}

	if (game.board.size == 0)
		startSplash(width, height)
	else
		sudokuGui(width, height, loadFromStorage = true)
}