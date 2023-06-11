package co.netguru.baby.monitor.client.feature.communication.pairing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.databinding.FragmentFailedDevicesConnectingBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen

class ConnectingDevicesFailedFragment : BaseFragment(R.layout.fragment_failed_devices_connecting) {
    override val screen: Screen = Screen.CONNECTION_FAILED
    private lateinit var binding : FragmentFailedDevicesConnectingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFailedDevicesConnectingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.configurationFailedTryAgainButton.setOnClickListener {
            findNavController().navigate(R.id.connectionFailedToServiceDiscovery)
        }
        setupOnBackPressedHandling()
    }

    private fun setupOnBackPressedHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.connectionFailedToServiceDiscovery)
            }
        })
    }
}
