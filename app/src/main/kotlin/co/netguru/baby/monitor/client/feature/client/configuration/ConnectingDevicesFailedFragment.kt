package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_failed_devices_connecting.*

class ConnectingDevicesFailedFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_failed_devices_connecting

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurationFailedTryAgainButton.setOnClickListener {
            findNavController().popBackStack(R.id.configuration, false)
        }
    }
}
