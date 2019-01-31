package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_connecting_setup_information.*

class SetupInformationFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_connecting_setup_information

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionInformationMbtn.setOnClickListener {
            findNavController().navigate(R.id.setupInformationToServer)
            requireActivity().finish()
        }
    }
}
