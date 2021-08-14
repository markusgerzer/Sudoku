package gui

import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*


data class MenuEntry(
    val string: String,
    val callback: (() -> Unit)?
)


inline fun popUpMenu(
    rootContainer: Container,
    entryHeight: Double,
    callback: @ViewDslMarker() (PopUpMenu.() -> Unit) = {}
) = PopUpMenu(
    rootContainer.width,
    rootContainer.height,
    entryHeight
).addTo(rootContainer, callback)

class PopUpMenu(
    override var width: Double,
    override var height: Double,
    val entryHeight: Double
): Container() {
    var defaultCallback: (() -> Unit)? = { removeFromParent() }

    var entries = listOf<MenuEntry>()
        set(value) {
            field = value
            drawEntries()
        }

    val rect = solidRect(width, height) {
        alpha = 0.7
        onClick {
            defaultCallback?.let { it1 -> it1() }
        }
    }

    private fun drawEntries() {
        container {
            var yEntries = 0.0
            Array(entries.size) {
                text(entries[it].string) {
                    y = yEntries
                    color = theme.fontDefault
                    fontSize = 0.5 * entryHeight
                    yEntries += entryHeight
                }
            }

            onClick {
                val yClicked = it.currentPosLocal.y
                val entryHeight = height / entries.size
                val idx = (yClicked / entryHeight).toInt()
                this@PopUpMenu.removeFromParent()
                entries[idx].callback?.let { it() }
            }

            centerOn(rect)
        }
    }

}