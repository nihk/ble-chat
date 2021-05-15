package nick.template.navigation

object AppNavGraph {
    val id = IdGenerator.next()

    object Destinations {
        val chatList = IdGenerator.next()
        val conversation = IdGenerator.next()
    }

    object Actions {
        val toConversation = IdGenerator.next()
    }
}
