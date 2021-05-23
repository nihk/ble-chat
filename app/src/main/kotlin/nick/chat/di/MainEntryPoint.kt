package nick.chat.di

import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import nick.chat.ui.MainViewModel

@EntryPoint
@InstallIn(ActivityComponent::class)
interface MainEntryPoint {
    val fragmentFactory: FragmentFactory
    val navController: NavController
    val mainViewModelFactory: MainViewModel.Factory
}
