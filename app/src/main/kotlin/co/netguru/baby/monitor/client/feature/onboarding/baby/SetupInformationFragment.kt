package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.databinding.FragmentConnectingSetupInformationBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen

class SetupInformationFragment : BaseFragment(R.layout.fragment_connecting_setup_information) {
    override val screen: Screen = Screen.SETUP_INFORMATION
    private lateinit var binding : FragmentConnectingSetupInformationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConnectingSetupInformationBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.connectionInformationMbtn.setOnClickListener {
            findNavController().navigate(R.id.setupInformationToServer)
            requireActivity().finish()
        }
    }
}
