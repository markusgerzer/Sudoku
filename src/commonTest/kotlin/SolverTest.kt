import sudoku.Board
import sudoku.Solver
import sudoku.Sudoku
import kotlin.test.Test
import kotlin.test.assertContentEquals

fun String.toBoard(
    blockSizeX: Int,
    blockSizeY: Int
): Board {
    val blockSize = blockSizeX * blockSizeY
    val values = ('0'..'9').toList() + ('A'..'Z').toList().take(blockSize + 1)
    val str = this.filter { it in values }
    val arr = IntArray(blockSize * blockSize) {
        str[it].toString().toInt(blockSize + 1)
    }
    return Board(blockSizeX, blockSizeY, arr)
}

class SolverTest {
    private val strEasyBoard = """
        000  910  002
        001  038  504
        580  200  000

        650  003  000
        103  409  000
        029  060  000

        000  340  000
        008  000  000
        302  001  090
        """.trimIndent()

    private val easyBoard = Board(
        3,
        3,
        intArrayOf(
            0, 0, 0, 9, 1, 0, 0, 0, 2,
            0, 0, 1, 0, 3, 8, 5, 0, 4,
            5, 8, 0, 2, 0, 0, 0, 0, 0,

            6, 5, 0, 0, 0, 3, 0, 0, 0,
            1, 0, 3, 4, 0, 9, 0, 0, 0,
            0, 2, 9, 0, 6, 0, 0, 0, 0,

            0, 0, 0, 3, 4, 0, 0, 0, 0,
            0, 0, 8, 0, 0, 0, 0, 0, 0,
            3, 0, 2, 0, 0, 1, 0, 9, 0
        )
    )
    private val strEasyBoardSolved = """
        437  915  862
        291  638  574
        586  274  139
           
        654  823  917
        173  459  286
        829  167  453

        915  346  728
        768  592  341
        342  781  695
        """.trimIndent()

    private val easyBoardSolved = listOf(
        4, 3, 7,  9, 1, 5,  8, 6, 2,
        2, 9, 1,  6, 3, 8,  5, 7, 4,
        5, 8, 6,  2, 7, 4,  1, 3, 9,

        6, 5, 4,  8, 2, 3,  9, 1, 7,
        1, 7, 3,  4, 5, 9,  2, 8, 6,
        8, 2, 9,  1, 6, 7,  4, 5, 3,

        9, 1, 5,  3, 4, 6,  7, 2, 8,
        7, 6, 8,  5, 9, 2,  3, 4, 1,
        3, 4, 2,  7, 8, 1,  6, 9, 5
    )

    private val normalBoard = """
        801 006 405
        000 000 037
        054 000 080
        
        000 408 000
        002 000 000
        300 010 000
        
        200 001 070
        040 030 000
        005 070 020
    """.trimIndent().toBoard(3, 3)

    private val normalBoardSolved = """
        831 726 495
        926 845 137
        754 193 286
        
        579 468 312
        412 359 768
        368 217 549
        
        293 581 674
        647 932 851
        185 674 923
    """.trimIndent().toBoard(3, 3)

    private val difficultBoard = """
        653 040 000
        000 002 605
        000 000 000
        
        014 095 000
        000 070 001
        700 034 006
        
        900 050 400
        000 000 020
        076 000 090
    """.trimIndent().toBoard(3, 3)

    private val difficultBoardSolved = """
        653 741 289
        149 382 675
        287 569 314
        
        814 695 732
        365 278 941
        792 134 856
        
        921 857 463
        438 916 527
        576 423 198
    """.trimIndent().toBoard(3, 3)

    private val professionalBoard = """
        021 730 000
        000 000 403
        600 100 000
        
        040 009 100
        900 460 030
        000 000 000
        
        000 000 000
        000 007 608
        008 201 047
    """.trimIndent().toBoard(3, 3)

    private val professionalBoardSolved = """
        421 738 569
        785 926 413
        639 154 782
        
        843 579 126
        917 462 835
        256 813 974
        
        374 685 291
        192 347 658
        568 291 347
    """.trimIndent().toBoard(3, 3)

    @Test
    fun testStringToBord() {
        assertContentEquals(
            easyBoardSolved,
            strEasyBoardSolved.toBoard(3, 3).data.toList()
        )

        assertContentEquals(
            easyBoard.data,
            strEasyBoard.toBoard(3, 3).data
        )
    }


    @Test
    fun testEasyBoard() {
        val sudoku = Sudoku(easyBoard)
        val solver = Solver(sudoku)

        solver.solve()
        assertContentEquals(easyBoardSolved, sudoku.solvedBoard)
    }


    @Test
    fun testNormalBoard() {
        val sudoku = Sudoku(normalBoard)
        val solver = Solver(sudoku)

        solver.solve()
        assertContentEquals(normalBoardSolved.data.toList(), sudoku.solvedBoard)
    }


    @Test
    fun testDifficultBoard() {
        val sudoku = Sudoku(difficultBoard)
        val solver = Solver(sudoku)

        solver.solve()
        assertContentEquals(difficultBoardSolved.data.toList(), sudoku.solvedBoard)
    }


    @Test
    fun testProfessionalBoard() {
        val sudoku = Sudoku(professionalBoard)
        val solver = Solver(sudoku)

        solver.solve()
        assertContentEquals(professionalBoardSolved.data.toList(), sudoku.solvedBoard)
    }
}