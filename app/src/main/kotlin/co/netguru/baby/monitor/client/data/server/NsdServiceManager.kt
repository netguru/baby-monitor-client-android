package co.netguru.baby.monitor.client.data.server

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class NsdServiceManager @Inject constructor(private val nsdManager: NsdManager) {

    private val nsdServiceListener by lazy {
        object : NsdManager.RegistrationListener {
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) =
                Timber.e("Baby Monitor Service unregistration failed")

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) = Unit

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) =
                Timber.e("Baby Monitor Service registration failed")

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) =
                Timber.d("Baby Monitor Service registered")
        }
    }

    internal fun registerService() {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            port = SERVICE_PORT
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdServiceListener)
    }

    internal fun unregisterService() {
        nsdManager.unregisterService(nsdServiceListener)
    }

    companion object {
        private const val SERVICE_NAME = "Baby Monitor Service"
        private const val SERVICE_TYPE = "_http._tcp."
        private const val SERVICE_PORT = 5004
    }
}
