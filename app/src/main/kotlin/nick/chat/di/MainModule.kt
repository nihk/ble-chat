package nick.chat.di

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap
import nick.chat.R
import nick.chat.ui.AppFragmentFactory
import nick.chat.conversation.ConversationFragment
import nick.chat.chatlist.DefaultOpenConversationCallback
import nick.chat.chatlist.ChatListFragment
import nick.chat.chatlist.OpenConversationCallback

@Module
@InstallIn(ActivityComponent::class)
abstract class MainModule {

    companion object {
        @Provides
        fun navController(activity: Activity): NavController {
            val navHostFragment = (activity as AppCompatActivity).supportFragmentManager
                .findFragmentById(R.id.navHostContainer) as NavHostFragment
            return navHostFragment.navController
        }
    }

    @Binds
    @IntoMap
    @FragmentKey(ChatListFragment::class)
    abstract fun chatListFragment(chatListFragment: ChatListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ConversationFragment::class)
    abstract fun conversationFragment(conversationFragment: ConversationFragment): Fragment

    @Binds
    abstract fun fragmentFactory(appFragmentFactory: AppFragmentFactory): FragmentFactory

    @Binds
    abstract fun openChatCallback(openChatCallback: DefaultOpenConversationCallback): OpenConversationCallback
}
