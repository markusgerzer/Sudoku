package gui

var `$` = Strings.DE


enum class Strings(
    val startSplash_lang: String,
    val startSplash_title: String,
    val startSplash_game: String,
    val startSplash_editor: String,
    val startMenu_msg0: String,
    val startMenu_msg1: String,
    val noteButtonConfig_textEditor: String,
    val noteButtonConfig_textGame: String,
    val gameButtonConfig_textEditor: String,
    val gameButtonConfig_textGame: String,
    val gameButtonConfig_editorMenuEntryNew: String,
    val gameButtonConfig_editorMenuEntrySolve: String,
    val gameButtonConfig_editorMenuEntryReset: String,
    val gameButtonConfig_gameMenuEntryNew: String,
    val gameButtonConfig_gameMenuEntryTip: String,
    val gameButtonConfig_gameMenuEntryReset: String,
    val SudokuGui_solvedMsg: String,
    val SudokuGui_editorNotSolvableMsg: String,
) {
    EN(
    "Language: English",
    "Sudoku",
    "Game",
    "Editor",
    "Creating new Sudoku.\nPlease wait...",
    "Choose size",
    "Candidates",
    "Note",
    "Editor",
    "Game",
    "New",
    "Solve",
    "Reset",
    "New",
    "Tip",
    "Reset",
    "Sudoku solved!",
    "Sudoku not [unique] solvable!",
    ),

    DE(
    "Sprache: Deutsch",
    "Sudoku",
    "Spiel",
    "Editor",
    "Erstelle neues Sudoku.\nBitte warten...",
    "Wähle die Größe",
    "Kandidaten",
    "Notiz",
    "Editor",
    "Spiel",
    "Neu",
    "Lösen",
    "Zurücksetzen",
    "Neu",
    "Tip",
    "Zurücksetzen",
    "Sudoku gelöst!",
    "Sudoku ist nicht [eindeutig] lösbar!",
    );

    fun next() = values()[(ordinal + 1) % values().size]
}