package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.databinding.FragmentAllDoneBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen

class AllDoneFragment : BaseFragment(R.layout.fragment_all_done) {
    override val screen: Screen = Screen.ALL_DONE
    private lateinit var binding: FragmentAllDoneBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllDoneBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            addDoneCtrl.setOnClickListener {
                findNavController().navigate(R.id.allDoneToClientHome)
                requireActivity().finish()
            }
            allDoneBackIv.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}
