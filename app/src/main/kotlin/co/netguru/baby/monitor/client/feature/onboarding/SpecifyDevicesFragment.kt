package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_specify_devices.*

class SpecifyDevicesFragment: BaseFragment() {
    override val layoutResource = R.layout.fragment_specify_devices

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        specifyDeviceDescriptionTv.text = Html.fromHtml(getString(R.string.sync_description))
        specifyDeviceBtn.setOnClickListener {
            findNavController().navigate(R.id.specifyDeviceToWelcome)
        }
    }
}
