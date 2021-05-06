package nick.template.navigation

object AppNavGraph {
    val id = IdGenerator.next()

    object Destinations {
        val devices = IdGenerator.next()
        val chat = IdGenerator.next()
    }

    object Actions {
        val toChat = IdGenerator.next()
    }
}
