package co.netguru.baby.monitor.client.feature.welcome

import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.onboarding.baby.WifiReceiver
import kotlinx.android.synthetic.main.fragment_welcome.*

class WelcomeFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_welcome

    private val wifiReceiver by lazy { WifiReceiver() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        babyCtl.setOnClickListener {
            findNavController().navigate(getNavControllerDestination())
        }
        parentCtl.setOnClickListener {
            findNavController().navigate(R.id.actionWelcomeToClientHomeActivity)
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        requireContext().registerReceiver(wifiReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(wifiReceiver)
    }

    private fun getNavControllerDestination() =
            if (wifiReceiver.isWifiConnected.value != true) {
                R.id.actionWelcomeToConnecting
            } else {
                R.id.actionWelcomeToPermissions
            }
}
