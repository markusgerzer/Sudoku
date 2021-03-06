package gui

import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import game
import gui.Direction.*
import storage.Storage
import storage.Storage.dataStorage
import sudoku.Solver
import kotlin.math.min


inline fun Container.sudokuGui(
    width: Double,
    height: Double,
    editorMode: Boolean = false,
    loadFromStorage: Boolean = false,
    callback: @ViewDslMarker SudokuGui.() -> Unit = {}
) = SudokuGui(
    width,
    height,
    editorMode,
    loadFromStorage
).addTo(this, callback)


class SudokuGui(
    override var width: Double,
    override var height: Double,
    editorMode: Boolean = false,
    loadFromStorage: Boolean = false
): Container(), Storage.Storable {

    override val storageKey = "SudokuGui"
    private var editorMode by dataStorage(editorMode, loadFromStorage)

    val landscape = width > height

    private val pxCell = if (landscape) {
            min(
                height / (game.blockSize + 1),
                width / (game.blockSize + 5)
            )
        } else {
            min(
                width / (game.blockSize + 1),
                height / (game.blockSize + 5)
            )
        }
    private val pxPadding = 0.03 * pxCell
    private val pxBackgroundX = pxCell * game.blockSize + pxPadding * (game.blockSize + game.blockSizeY * 2 + 3)
    private val pxBackgroundY = pxCell * game.blockSize + pxPadding * (game.blockSize + game.blockSizeX * 2 + 3)
    private val pxEntryHeight = min(width, height) / 8

    private val backgroundCell = solidRect(pxBackgroundX, pxBackgroundY, theme.border) {
        if (landscape) {
            x = (this@SudokuGui.width - (pxBackgroundX + 3.5 * pxCell)) / 2
            y = (this@SudokuGui.height - pxBackgroundY) / 2
        } else {
            x = (this@SudokuGui.width - pxBackgroundX) / 2
            y = (this@SudokuGui.height - (pxBackgroundY + 3.5 * pxCell)) / 2
        }
    }

    private val cells = Array(game.size) { idx ->
        cell(pxCell, pxCell) {
            val col = idx % game.blockSize
            val row = idx / game.blockSize
            x = backgroundCell.x + 3 * pxPadding + col * (pxCell +
                    pxPadding) + col / game.blockSizeX * pxPadding * 2
            y = backgroundCell.y + 3 * pxPadding + row * (pxCell +
                    pxPadding) + row / game.blockSizeY * pxPadding * 2

            onClick {
                val value =
                    if (Cell.stringValues[game[idx]] == valueButtons.currentValue) 0
                    else Cell.stringValues.indexOf(valueButtons.currentValue)

                if (!editorMode && noteButton.status == Button.Status.ON ) {
                    if (value in game.notes.getAt(idx)) game.notes.delAt(idx, value)
                    else game.notes.addAt(idx, value)
                } else
                    game[idx] = value

                draw(idx)
            }
        }
    }
    init {
        game.failedIndicesCallback = { oldFailedIndices, newFailedIndices ->
            for (index in oldFailedIndices - newFailedIndices)
                cells[index].failure = false
            for (index in newFailedIndices)
                cells[index].failure = true
        }

        if (editorMode)
            game.candidates.changedCallback = { oldCandidates, newCandidates ->
                for (i in 0 until game.size)
                    if (oldCandidates[i] != newCandidates[i])
                        cells[i].draw(i)
            }

        game.notes.changedCallback.add { oldNotes, newNotes ->
            for (i in 0 until game.size)
                if (oldNotes[i] != newNotes[i])
                    cells[i].draw(i)
        }
    }

    private val pxButton = (backgroundCell.width - (game.blockSize - 1) * pxPadding) / (game.blockSize)

    inner class ValueButtonConfig {
        val width =
            if (landscape) pxButton
            else (backgroundCell.width / game.blockSize * game.blockSize)
        val height =
            if (landscape) (backgroundCell.height / game.blockSize * game.blockSize)
            else pxButton
        val strings = Cell.stringValues.take(game.blockSize + 1).drop(1)
        val direction = if (landscape) VERTICAL else HORIZONTAL
        val x =
            if (landscape) backgroundCell.x + backgroundCell.width + pxButton
            else backgroundCell.x
        val y =
            if (landscape) backgroundCell.y
            else backgroundCell.y + backgroundCell.height + pxButton
    }
    private val valueButtons: ValueButtons = with(ValueButtonConfig()) {
        object : ValueButtons(
            width,
            height,
            strings,
            direction
        ), Storage.Storable {
            override val storageKey = "ValueButtons"
            override var currentValue by dataStorage(strings[0], loadFromStorage)

            init {
                set(buttons[currentIndex()])

                game.completedValuesCallback = {
                        oldCompletedValues, newCompletedValues ->
                    for (value in oldCompletedValues - newCompletedValues - 0)
                        buttons[value - 1].mark =
                            Button.Status.OFF
                    for (value in newCompletedValues - 0)
                        buttons[value - 1].mark =
                            Button.Status.ON

                    if (buttons[currentIndex()].mark == Button.Status.ON) activateNext()
                }
                x = this@with.x
                y = this@with.y
            }
        }.addTo(this@SudokuGui)
    }

    private val noteButton =
        object : IconToggleButton(pxButton, pxButton, Icons.notes), Storage.Storable {
            override val storageKey = "NoteButton"

            private var status1 by dataStorage(Status.OFF, loadFromStorage) {
                enumValueOf(it as String) as Status
            }
            override var status
                get() = status1
                set(value) {
                    super.status = value
                    status1 = value
                }
            init {
                status = status1
                x = if (landscape) backgroundCell.x + backgroundCell.width + 2.5 * pxButton
                    else backgroundCell.x
                y = if (landscape) backgroundCell.y
                    else backgroundCell.y + backgroundCell.height + 2.5 * pxButton
            }
        }.addTo(this)
    init {
        noteButton.forEachChild {
            it.onClick {
                drawCells()
            }
        }
    }

    private val menuButton =
        object : IconButton(pxButton, pxButton, Icons.menu) {
            init {
                onClick {
                    popUpMenu(
                        this@SudokuGui,
                        pxEntryHeight
                    ) {
                        entries =
                            if (this@SudokuGui.editorMode) listOf(
                                MenuEntry(`$`.gameButtonConfig_editorMenuEntryNew) { exitGame() },
                                MenuEntry(`$`.gameButtonConfig_editorMenuEntrySolve) { solveGame() },
                                MenuEntry(`$`.gameButtonConfig_editorMenuEntryReset) { resetGame() }
                            ) else listOf(
                                MenuEntry(`$`.gameButtonConfig_gameMenuEntryNew) { exitGame() },
                                MenuEntry(`$`.gameButtonConfig_gameMenuEntryTip) { tipGame() },
                                MenuEntry(`$`.gameButtonConfig_gameMenuEntryReset) { resetGame() }
                            )
                    }
                }
                x =
                    if (landscape) backgroundCell.x + backgroundCell.width + 2.5 * pxButton
                    else backgroundCell.x + backgroundCell.width - width
                y =
                    if (landscape)  backgroundCell.y + backgroundCell.height - height
                    else backgroundCell.y + backgroundCell.height + 2.5 * pxButton
            }
        }.addTo(this)

    init {
        if (!editorMode) {
            game.solvedCallback = {
                popUpMenu(this, pxEntryHeight) {
                    defaultCallback = ::exitGame
                    entries = listOf(
                        MenuEntry(
                            `$`.SudokuGui_solvedMsg,
                            ::exitGame
                        )
                    )
                }
            }
        }

        game.reinitialize()
        drawCells()
    }


    private fun exitGame() {
        removeChildren()
        startSplash(width, height)
    }

    private fun solveGame() {
        for (i in 0 until game.size)
            if (game[i] != 0) game.immutableIndices.add(i)

        val solver = Solver(game)
        if (!solver.uniqueSolution.solve()) {
            game.reset()
            game.immutableIndices.clear()

            popUpMenu(this, pxEntryHeight) {
                entries = listOf(
                    MenuEntry(
                        `$`.SudokuGui_editorNotSolvableMsg,
                        null
                    )
                )
            }
        }
        drawCells()
    }

    private fun tipGame() {
        val index = game.freeIndices().random()
        val value = game.solvedBoard?.get(index)
        game[index] = value ?: 0
        cells[index].draw(index)
    }

    private fun resetGame() {
        game.reset()
        game.notes.clear()
        for (i in 0 until game.size)
            cells[i].failure = false
        drawCells()
    }

    private fun drawCells() {
        for (i in 0 until game.size)
            cells[i].draw(i)
    }

    private fun Cell.draw(index: Int) {
        val color =
            when (index) {
                in game.immutableIndices -> theme.fontDefault
                in game.freeIndices() -> theme.fontCandidates
                else -> theme.fontVariable
            }

        if (index in game.freeIndices()) {
            if (editorMode && noteButton.status == Button.Status.ON)
                draw(game.candidates.getAt(index), color)
            else
                draw(game.notes.getAt(index), color)
        } else {
            draw(game[index], color)
        }
    }
}