package gui

import com.soywiz.korge.view.*
import com.soywiz.korim.color.RGBA
import game
import kotlin.math.max
import kotlin.math.min


inline fun Container.cell(
    width: Double,
    height: Double,
    callback: @ViewDslMarker Cell.() -> Unit = {}
) = Cell(width, height).addTo(this, callback)


class Cell(
    override var width: Double,
    override var height: Double
): Container() {

    private val rect = solidRect(width, height, theme.cell)
    private var content: View? = null


    var failure = false
        set(value) {
            if (value) rect.color = theme.cellError
            else rect.color = theme.cell
            field = value
        }

    private val fontSizeSingleContent = 0.8 * min(width, height)
    private val contentWidth = width / game.blockSizeX
    private val contentHeight = height / game.blockSizeY
    private val fontSizeMultiContent = 0.8 * min(width, height) / max(
        game.blockSizeX, game.blockSizeY)
    private val contentXPadding = (width - fontSizeMultiContent * game.blockSizeX) * .4
    private val contentYPadding = (height - fontSizeMultiContent * game.blockSizeY) * .2

    fun draw(value: Int, color: RGBA) {
        removeChild(content)
        if (value != 0) {
            val string = stringValues[value]
            content = text(string) {
                this.color = color
                fontSize = fontSizeSingleContent
                centerOn(rect)
            }
        }
    }

    fun draw(candidates: List<Int>, color: RGBA) {
        removeChild(content)
        if (candidates.isNotEmpty()) {
            val strings = candidates.associateWith { stringValues[it] }
            content = container {
                for (i in 0 until game.blockSize) {
                    strings[i + 1]?.let {
                        text (it) {
                            this.color = color
                            fontSize = fontSizeMultiContent
                            x = contentXPadding + i % game.blockSizeX * contentWidth
                            y = contentYPadding + i / game.blockSizeX * contentHeight
                        }
                    }
                }
            }
        }
    }

    companion object {
        val stringValues = listOf("") +
                (('1'..'9').toList() + ('A'..'Z').toList()).map(Char::toString)
    }
}