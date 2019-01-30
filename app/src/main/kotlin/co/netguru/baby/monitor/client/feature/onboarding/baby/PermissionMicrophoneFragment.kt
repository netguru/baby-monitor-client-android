package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.Manifest
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted

class PermissionMicrophoneFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_microphone_permission

    override fun onResume() {
        super.onResume()
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        findNavController().navigate(
                if (requireContext().allPermissionsGranted(Companion.permissions)) {
                    R.id.permissionMicrophoneToSetupInformation
                } else {
                    R.id.permissionMicrophoneToPermissionDenied
                }
        )
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 126

        private val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO
        )
    }
}
