package gui

import com.soywiz.korim.color.Colors

var theme = Lime

open class Theme {
    open val background = Colors.CADETBLUE
    open val border = Colors.DARKSLATEGRAY
    open val button = Colors.LIGHTSLATEGRAY
    open val cell = Colors.BEIGE
    open val cellError = Colors.RED
    open val fontDefault = Colors.BLACK
    open val fontHead = Colors.DIMGRAY
    open val fontVariable = Colors.DIMGRAY
    open val fontCandidates = Colors.DARKBLUE
}

object Default : Theme()

object Lime : Theme() {
    override val background = Colors["d9e78d"]
    override val border = Colors["7a8b1d"]
    override val button = Colors["cddf68"]
    override val cell = Colors["f2f7d9"]
}