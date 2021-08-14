package gui

import com.soywiz.korim.vector.format.SVG
import com.soywiz.korio.file.std.resourcesVfs

object Icons {
    lateinit var menu: SVG
    lateinit var notes: SVG
    lateinit var undo: SVG
    lateinit var redo: SVG

    suspend fun load() {
        menu = SVG(resourcesVfs["menu.svg"].readString())
        notes = SVG(resourcesVfs["pencil.svg"].readString())
        undo = SVG(resourcesVfs["action-undo.svg"].readString())
        redo = SVG(resourcesVfs["action-redo.svg"].readString())
    }
}
