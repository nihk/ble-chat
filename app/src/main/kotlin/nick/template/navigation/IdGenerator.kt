package nick.template.navigation

object IdGenerator {
    private var count = 1

    fun next(): Int = count++
}
