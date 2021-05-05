package nick.template.navigation

object AppNavGraph {
    val id = IdGenerator.next()

    object Destination {
        val devices = IdGenerator.next()
        val chat = IdGenerator.next()
    }
}
