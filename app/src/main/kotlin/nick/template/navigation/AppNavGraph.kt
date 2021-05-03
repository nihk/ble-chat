package nick.template.navigation

object AppNavGraph {
    val id = IdGenerator.next()

    object Destination {
        val bluetooth = IdGenerator.next()
        val permissions = IdGenerator.next()
    }
}
