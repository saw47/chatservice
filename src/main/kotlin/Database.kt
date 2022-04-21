object Database {
    private var counter: Long = -1
    fun createNumber(): Long {
        counter++
        return counter
    }

    var storage = mutableMapOf<Pair<Int, Int>, MutableList<PrivateMessage>>()
}