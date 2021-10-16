package sudoku




class SolutionTree() {
    class Node(
        val boardArray: IntArray,
        val index: Int,
        val values: List<Int>,
        val nextNodes: MutableList<Node?> = mutableListOf(),
        var depth: Int = 0
    )

    private val stack = mutableListOf<Node>()
    var rootNode: Node? = null
        private set
    var solutions = 0
        private set
    var depth = 0
        private set

    fun clear() {
        stack.clear()
        rootNode = null
        solutions = 0
        depth = 0
    }

    fun addLeaf(node: Node?) {
        if (node != null) {
            node.nextNodes.addAll(List(node.values.size) { null })
            solutions++
        }
        addNode(node)
    }

    fun addNode(node: Node?) {
        if (rootNode == null) rootNode = node
        if (stack.isNotEmpty()) {
            val lastStackItem = stack.last()
            lastStackItem.nextNodes.add(node)

            node?.let {
                it.depth = lastStackItem.depth + 1
                if (depth < it.depth) depth = it.depth
            }

            if (lastStackItem.nextNodes.size >= lastStackItem.values.size)
                stack.removeLast()
        }
        if(node != null && node.nextNodes.size < node.values.size) stack.add(node)
    }

    fun traversLevelOrder() = iterator<Node> {
        val queue = mutableListOf<Node>()
        rootNode?.let { queue.add(it) }
        while (queue.isNotEmpty()) {
            val currentNode = queue.removeFirst()
            queue.addAll(currentNode.nextNodes.filterNotNull())
            yield(currentNode)
        }
    }
}