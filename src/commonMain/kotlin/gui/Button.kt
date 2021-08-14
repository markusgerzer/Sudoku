package gui

import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korim.vector.format.SVG
import com.soywiz.korim.vector.render
import com.soywiz.korim.vector.toSvg
import gui.Button.Status.OFF
import gui.Button.Status.ON
import gui.Direction.HORIZONTAL
import kotlin.math.min


abstract class Button(
    final override var width: Double,
    final override var height: Double,
): Container() {
    enum class Status { ON, OFF }

    open var status = OFF
        set(value) {
            when (value) {
                ON -> {
                    inner.scaledWidth = width - 18.0
                    inner.scaledHeight = height - 18.0
                    inner.position(9.0, 9.0)
                }
                OFF -> {
                    inner.scaledWidth = width - 4.0
                    inner.scaledHeight = height - 4.0
                    inner.position(2.0, 2.0)
                }
            }
            field = value
        }

    var mark = OFF
        set(value) {
            when (value) {
                ON -> {
                    inner.color = theme.border
                }
                OFF -> {
                    inner.color = theme.button
                }
            }
            field = value
        }

    val border = roundRect(width, height, 2.0) {
        color = theme.border
    }

    val inner = roundRect(width - 4, height - 4, 2.0) {
        position(2.0, 2.0)
        color = theme.button
    }
}


inline fun Container.textButton(
    width: Double,
    height: Double,
    value: String,
    callback: @ViewDslMarker (TextButton.() -> Unit) = {}
) = TextButton(width, height, value).addTo(this, callback)
open class TextButton(
    width: Double,
    height: Double,
    val value: String,
): Button(width, height) {

    var text = text(value) {
        color = theme.fontDefault
        fontSize = 0.8 * min(inner.width, inner.height)
        centerOn(inner)
    }
}


inline fun Container.iconButton(
    width: Double,
    height: Double,
    icon: SVG,
    callback: @ViewDslMarker (IconButton.() -> Unit) = {}
) = IconButton(width, height, icon).addTo(this, callback)
open class IconButton(
    width: Double,
    height: Double,
    icon: SVG,
): Button(width, height) {
    private val iconScale = width * .67 / icon.dheight
    private val svg = icon.toSvg(iconScale)
    val image = image(SVG(svg).render()) {
        centerOn(inner)
    }
}


inline fun Container.iconToggleButton(
    width: Double,
    height: Double,
    icon: SVG,
    callback: @ViewDslMarker (IconToggleButton.() -> Unit) = {}
) = IconToggleButton(width, height, icon).addTo(this, callback)
open class IconToggleButton(
    width: Double,
    height: Double,
    icon: SVG,
): IconButton(width, height, icon) {
    init {
        forEachChild { it.onClick { toggleStatus() } }
    }

    fun toggleStatus() {
        if (status == ON) status = OFF
        else if (status == OFF) status = ON
    }
}


abstract class ValueButtons(
    final override var width: Double,
    final override var height: Double,
    values: List<String>,
    direction: Direction,
    padding: Double = width / values.size * 0.03,
): Container() {
    
    val buttonWidth: Double
    val buttonHeight: Double

    init {
        if (direction == HORIZONTAL) {
            buttonWidth = (width - (values.size - 1) * padding) / values.size
            buttonHeight = height
        } else {
            buttonWidth = width
            buttonHeight = (height - (values.size -1) * padding) / values.size
        }
    }

    val buttons = Array(values.size) {
        textButton(buttonWidth, buttonHeight, values[it]) {
            when (direction) {
                HORIZONTAL -> x = it * (buttonWidth + padding)
                Direction.VERTICAL -> y = it * (buttonHeight + padding)
            }
        }
    }

    abstract var currentValue: String
        protected set
    
    init {
        for (button in buttons)
            for (child in button.children)
                child.onClick { set(button) }
    }

    fun set(button: TextButton) {
        button.status = ON
        currentValue = button.value
        for (button1 in buttons)
            if (button1 != button)
                button1.status = OFF
    }

    fun currentIndex(): Int {
        for (i in buttons.indices)
            if (buttons[i].value == currentValue) return i
        throw IllegalStateException("$currentValue not found in ${List(buttons.size) { buttons[it].value }}")
    }

    fun activateNext() {
        for (i in 1 until buttons.size) {
            val button = buttons[(currentIndex() + i) % buttons.size]
            if (button.mark == OFF) {
                set(button)
                return
            }
        }
    }
}