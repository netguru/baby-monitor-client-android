package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import javax.inject.Inject

class ServerViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager
) : ViewModel() {

    internal fun registerNsdService() {
        nsdServiceManager.registerService()
    }

    internal fun unregisterNsdService() {
        nsdServiceManager.unregisterService()
    }

    override fun onCleared() {
        super.onCleared()
    }
}
