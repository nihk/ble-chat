package nick.template.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nick.template.R
import nick.template.databinding.MainFragmentBinding
import javax.inject.Inject

class MainFragment @Inject constructor(
    private val vmFactory: MainViewModel.Factory
) : Fragment(R.layout.main_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MainFragmentBinding.bind(view)
    }
}