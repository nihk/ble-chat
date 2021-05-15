package nick.template.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavActionBuilder
import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.fragment
import dagger.hilt.android.AndroidEntryPoint
import nick.template.R
import nick.template.di.MainEntryPoint
import nick.template.navigation.AppNavGraph
import nick.template.ui.conversation.ConversationFragment
import nick.template.ui.chatlist.ChatListFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.main_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        val entryPoint = entryPoint<MainEntryPoint>()
        supportFragmentManager.fragmentFactory = entryPoint.fragmentFactory
        super.onCreate(savedInstanceState)
        createNavGraph(entryPoint.navController)
    }

    private fun createNavGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            id = AppNavGraph.id,
            startDestination = AppNavGraph.Destinations.chatList
        ) {
            fragment<ChatListFragment>(AppNavGraph.Destinations.chatList) {
                action(AppNavGraph.Actions.toConversation) {
                    destinationId = AppNavGraph.Destinations.conversation
                    defaultAnimations()
                }
            }
            fragment<ConversationFragment>(AppNavGraph.Destinations.conversation)
        }
    }

    private fun NavActionBuilder.defaultAnimations() {
        navOptions {
            anim {
                enter = R.animator.nav_default_enter_anim
                exit = R.animator.nav_default_exit_anim
                popEnter = R.animator.nav_default_pop_enter_anim
                popExit = R.animator.nav_default_pop_exit_anim
            }
        }
    }
}
