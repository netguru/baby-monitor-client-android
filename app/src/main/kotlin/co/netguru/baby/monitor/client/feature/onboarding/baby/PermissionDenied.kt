package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.PERMISSION_DENIED
import kotlinx.android.synthetic.main.fragment_denied_permission.*

class PermissionDenied : BaseFragment() {
    override val layoutResource = R.layout.fragment_denied_permission
    override val screenName: String = PERMISSION_DENIED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deniedRetryButtonCtrl.setOnClickListener {
            findNavController().popBackStack(R.id.connectWiFi, false)
        }
        deniedSureButtonCtrl.setOnClickListener {
            requireActivity().finish()
        }
    }
}
