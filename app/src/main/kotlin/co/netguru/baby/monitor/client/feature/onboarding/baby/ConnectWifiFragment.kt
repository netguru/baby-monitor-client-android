package co.netguru.baby.monitor.client.feature.onboarding.baby

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import kotlinx.android.synthetic.main.fragment_connect_wifi.*

class ConnectWifiFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_connect_wifi

    private val wifiReceiver by lazy { WifiReceiver() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionConnectWiFiCtrl.setOnClickListener {
            if (wifiReceiver.isWifiConnected.value == true) {
                findNavController().navigate(
                        when {
                            requireContext().allPermissionsGranted(allPermissions) -> R.id.connectWiFiToSetupInformation
                            requireContext().allPermissionsGranted(cameraPermission) -> R.id.connectWiFiToPermissionMicrophone
                            else -> R.id.connectWiFiToPermissionCamera
                        }
                )
            } else {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
        wifiReceiver.isWifiConnected.observe(this, Observer { isConnected ->
            connectionConnectWiFiTv.text = if (isConnected == true) {
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
