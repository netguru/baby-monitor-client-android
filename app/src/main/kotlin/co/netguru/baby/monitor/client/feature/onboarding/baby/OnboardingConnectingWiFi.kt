package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import kotlinx.android.synthetic.main.fragment_connecting_wifi.*

class OnboardingConnectingWiFi : Fragment() {

    private val wifiReceiver by lazy { WifiReceiver() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_connecting_wifi, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingConnectingBackTv.setOnClickListener {
            requireActivity().onBackPressed()
        }
        connectionConnectWiFiMbtn.setOnClickListener {
            if (wifiReceiver.isWifiConnected.value == true) {
                findNavController().navigate(R.id.onboardingPermissions)
            } else {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
        wifiReceiver.isWifiConnected.observe(this, Observer { isConnected ->
            connectionConnectWiFiMbtn.text = if (isConnected == true) {
                connectionConnectWiFiMbtn.callOnClick()
                getString(R.string.connect_wifi_connected)
            } else {
                getString(R.string.connect_to_wi_fi)
            }
        })
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
}
