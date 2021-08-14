package gui

import com.soywiz.klock.milliseconds
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korio.async.*
import game
import kotlinx.coroutines.GlobalScope
import sudoku.Sudoku.Companion.blankSudoku
import sudoku.Sudoku.Companion.createSudoku
import kotlin.math.min


fun Container.startSplash(width: Double, height: Double) {
    fun start(editorMode: Boolean) {
        removeChildren()
        startMenu(width, height, editorMode)
    }

    container {
        val h = min(width, height)

        val title = text(`$`.startSplash_title) {
            textSize = h / 5
            color = theme.fontHead
            x = (width - this.width) / 2
        }

        val menu = container {
            val textGame = text(`$`.startSplash_game) {
                textSize = h / 10
                color = theme.fontDefault
                x = (this@container.width - this.width) / 2
            }
            text(`$`.startSplash_editor) {
                textSize = h / 11
                color = theme.fontDefault
                x = (this@container.width - this.width) / 2
                y = textGame.y + textGame.height + h / 10
            }
            onClick {
                start(it.currentPosLocal.y / this.height > 0.5)
            }
            x = (width - this.width) / 2
            y = title.y + title.height + h / 10
        }

        container {
            val lang = text(`$`.startSplash_lang) {
                textSize = h / 15
                color = theme.fontDefault

            }
            children.forEach { _ ->
                onClick {
                    `$` = `$`.next()
                    this@startSplash.removeChildren()
                    this@startSplash.startSplash(width, height)
                }
            }
            x = (width - this.width) / 2
            y = menu.y + menu.height + h / 10
        }

        y = (height - this.height) / 2
    }
}


fun Container.startMenu(width: Double, height: Double, editorMode: Boolean) {
    val h = min(width, height)

    fun Container.start(blockSizeX: Int, blockSizeY: Int) {
        removeChildren()

        GlobalScope.launch {
            text(`$`.startMenu_msg0) {
                textSize = h / 14
                color = theme.fontDefault
                x = (width - this.width) / 2
                y = (height - this.height) / 2
            }

            delay(500.milliseconds)

            game =
                if (editorMode) blankSudoku(blockSizeX, blockSizeY)
                else createSudoku(blockSizeX, blockSizeY)

            removeChildren()

            sudokuGui(width, height, editorMode)
        }
    }

    val text = text(`$`.startMenu_msg1) {
        textSize = h / 12
        color = if (editorMode) theme.fontCandidates else theme.fontDefault
        x = (width - this.width) / 2
        y = h / 10
    }

    val menuEntries = listOf(
        MenuEntry("2 X 2") { start(2, 2) },
        //gui.MenuEntry("2 X 3") { start(2, 3) },
        MenuEntry("3 X 2") { start(3, 2) },
        MenuEntry("4 X 2") { start(4, 2) },
        MenuEntry("3 X 3") { start(3, 3) },
        //gui.MenuEntry("3 X 4") { start(3, 4) },
        //gui.MenuEntry("4 X 3") { start(4, 3) },
        //gui.MenuEntry("4 X 4") { start(4, 4) }
    )

    container {
        for (i in menuEntries.indices) {
            text(menuEntries[i].string) {
                textSize = h / 14
                color = theme.fontDefault
                x = (this@container.width - this.width) / 2
                y = this@container.y + h / 10 * i
            }
        }

        onClick {
            val yClicked = it.currentPosLocal.y
            val entryHeight = this.height / menuEntries.size
            val idx = (yClicked / entryHeight).toInt()
            menuEntries[idx].callback?.let { it() }
        }

        x = (width - this.width) / 2
        y = text.y + text.height+ h / 10
    }

}