package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.Manifest
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.feature.analytics.Screen

class PermissionCameraFragment : BaseFragment(R.layout.fragment_camera_permission) {
    override val screen: Screen = Screen.PERMISSION_CAMERA

    override fun onResume() {
        super.onResume()
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        findNavController().navigate(
                if (requireContext().allPermissionsGranted(Companion.permissions)) {
                    R.id.permissionCameraToPermissionMicrophone
                } else {
                    R.id.permissionCameraToPermissionDenied
                }
        )
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                Manifest.permission.CAMERA
        )
    }
}
