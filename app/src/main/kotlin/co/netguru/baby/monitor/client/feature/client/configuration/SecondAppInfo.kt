package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_second_app_info.*

class SecondAppInfo : BaseFragment() {
    override val layoutResource = R.layout.fragment_second_app_info

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        secondAppButtonCtrl.setOnClickListener {
            findNavController().navigate(R.id.secondAppInfoToConfiguration)
        }

        secondAppInfoBackIv.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
