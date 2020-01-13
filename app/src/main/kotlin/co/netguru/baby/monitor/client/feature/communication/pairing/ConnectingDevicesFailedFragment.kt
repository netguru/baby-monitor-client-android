package co.netguru.baby.monitor.client.feature.communication.pairing

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_failed_devices_connecting.*

class ConnectingDevicesFailedFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_failed_devices_connecting

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurationFailedTryAgainButton.setOnClickListener {
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
