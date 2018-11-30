package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.extensions.allPermissionsGranted
import kotlinx.android.synthetic.main.fragment_connecting_permissions.*

class OnboardingPermissions : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_connecting_permissions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onboardingPermissionBackTv.setOnClickListener { requireActivity().onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            findNavController().navigate(R.id.actionPermissionToSetupInfo)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(Companion.permissions)) {
            findNavController().navigate(R.id.actionPermissionToSetupInfo)
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
        )
    }
}
