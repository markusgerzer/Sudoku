package sudoku

class SolverTreeNode (
    val boardArray: IntArray,
    val lastIndex: Int,
    val nextNodes: MutableList<SolverTreeNode>
)