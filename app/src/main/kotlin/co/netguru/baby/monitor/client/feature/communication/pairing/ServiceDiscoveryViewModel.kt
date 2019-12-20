package co.netguru.baby.monitor.client.feature.communication.pairing

import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdState
import javax.inject.Inject

class ServiceDiscoveryViewModel @Inject constructor(
    private val nsdServiceManager: NsdServiceManager
) : ViewModel() {

    private var multicastLock: WifiManager.MulticastLock? = null
    val nsdStateLiveData: LiveData<NsdState> = nsdServiceManager.nsdStateLiveData

    internal fun discoverNsdService(
        wifiManager: WifiManager
    ) {
        acquireMulticastLock(wifiManager)
        nsdServiceManager.discoverService()
    }

    private fun acquireMulticastLock(wifiManager: WifiManager) {
        // Pixel workaround for discovering services
        multicastLock = wifiManager.createMulticastLock(MUTLICAST_LOCK_TAG)
        multicastLock?.setReferenceCounted(true)
        multicastLock?.acquire()
    }

    internal fun stopNsdServiceDiscovery() {
        releaseMulticastLock()
        nsdServiceManager.stopServiceDiscovery()
    }

    private fun releaseMulticastLock() {
        multicastLock?.release()
        multicastLock = null
    }

    override fun onCleared() {
        nsdServiceManager.stopServiceDiscovery()
        super.onCleared()
    }

    companion object {
        internal const val MUTLICAST_LOCK_TAG = "multicastLock"
    }
}
