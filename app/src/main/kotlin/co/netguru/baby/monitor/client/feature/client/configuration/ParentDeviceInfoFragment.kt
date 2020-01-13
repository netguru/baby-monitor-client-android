package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.analytics.Screen
import kotlinx.android.synthetic.main.fragment_parent_device_info.*

class ParentDeviceInfoFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_parent_device_info
    override val screen: Screen = Screen.PARENT_DEVICE_INFO

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
