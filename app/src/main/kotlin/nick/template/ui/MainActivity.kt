package nick.template.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.fragment
import dagger.hilt.android.AndroidEntryPoint
import nick.template.R
import nick.template.di.MainEntryPoint
import nick.template.navigation.AppNavGraph

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
            startDestination = AppNavGraph.Destination.main
        ) {
            fragment<MainFragment>(AppNavGraph.Destination.main)
        }
    }
}