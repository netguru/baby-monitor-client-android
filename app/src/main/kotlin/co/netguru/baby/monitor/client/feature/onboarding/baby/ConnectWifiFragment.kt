package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.CONNECT_WIFI
import kotlinx.android.synthetic.main.fragment_connect_wifi.*

class ConnectWifiFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_connect_wifi
    override val screenName: String = CONNECT_WIFI

    private val wifiReceiver by lazy { WifiReceiver() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiConnectionButton.setOnClickListener {
            if (wifiReceiver.isWifiConnected.value?.fetchData() == true) {
                findNavController().navigate(
                    when {
                        requireContext().allPermissionsGranted(allPermissions)
                        -> R.id.connectWiFiToSetupInformation
                        requireContext().allPermissionsGranted(cameraPermission)
                        -> R.id.connectWiFiToPermissionMicrophone
                        else -> R.id.connectWiFiToPermissionCamera
                    }
                )
            } else {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
        wifiReceiver.isWifiConnected.observe(this, Observer { isConnected ->
            wifiConnectionButton.text = if (isConnected?.fetchData() == true) {
                getString(R.string.connect_wifi_connected)
            } else {
                getString(R.string.connect_to_wi_fi)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(wifiReceiver, WifiReceiver.intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(wifiReceiver)
    }

    companion object {
        private val allPermissions = arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
        private val cameraPermission = arrayOf(
                Manifest.permission.CAMERA
        )
    }
}
