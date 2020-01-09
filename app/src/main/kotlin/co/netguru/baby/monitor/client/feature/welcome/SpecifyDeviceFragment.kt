package co.netguru.baby.monitor.client.feature.welcome

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.SPECIFY_DEVICE
import kotlinx.android.synthetic.main.fragment_specify_device.*

class SpecifyDeviceFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_specify_device
    override val screenName: String = SPECIFY_DEVICE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        babyCtl.setOnClickListener {
            findNavController().navigate(R.id.specifyDeviceToFeatureD)
        }
        parentCtl.setOnClickListener {
            findNavController().navigate(R.id.specifyDeviceToParentDeviceInfo)
        }
    }
}
