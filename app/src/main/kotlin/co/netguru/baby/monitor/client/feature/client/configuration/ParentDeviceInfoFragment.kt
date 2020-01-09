package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.PARENT_DEVICE_INFO
import kotlinx.android.synthetic.main.fragment_parent_device_info.*

class ParentDeviceInfoFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_parent_device_info
    override val screenName: String = PARENT_DEVICE_INFO

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        secondAppButtonCtrl.setOnClickListener {
            findNavController().navigate(R.id.secondAppInfoToServiceDiscovery)
        }

        secondAppInfoBackIv.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
