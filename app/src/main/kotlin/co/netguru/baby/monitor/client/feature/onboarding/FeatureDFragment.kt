package co.netguru.baby.monitor.client.feature.onboarding

import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.onboarding.baby.WifiReceiver
import kotlinx.android.synthetic.main.fragment_feature_d.*

class FeatureDFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_feature_d

    private val wifiReceiver by lazy { WifiReceiver() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        featureDNextBtn.setOnClickListener {
            findNavController().navigate(R.id.featureDToConnecting)
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
}
