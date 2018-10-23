package co.netguru.baby.monitor.client.data.server

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class NsdServiceManager @Inject constructor(
    private val nsdManager: NsdManager, private val configurationRepository: ConfigurationRepository
) {

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

    private val nsdDiscoveryListener by lazy {
        object : NsdManager.DiscoveryListener {
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {

                if (serviceInfo.serviceName == SERVICE_NAME) {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Timber.e("Baby Monitor Service resolve failed")
                            onServiceConnectedListener?.onServiceConnectionError()
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            appendNewAddress(serviceInfo.host.hostAddress, serviceInfo.port)
                            onServiceConnectedListener?.onServiceConnected()
                        }
                    })
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) = Unit

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) =
                Timber.e("Baby Monitor Service discovery failed")

            override fun onDiscoveryStarted(serviceType: String?) =
                Timber.d("Baby Monitor Service discovery started")

            override fun onDiscoveryStopped(serviceType: String?) = Unit

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) =
                Timber.e("Baby Monitor Service failed lost")
        }
    }

    private var onServiceConnectedListener: OnServiceConnectedListener? = null

    internal fun registerService() {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            port = App.PORT
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdServiceListener)
    }

    internal fun unregisterService() = nsdManager.unregisterService(nsdServiceListener)

    internal fun discoverService(onServiceConnectedListener: OnServiceConnectedListener) {
        this.onServiceConnectedListener = onServiceConnectedListener
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, nsdDiscoveryListener)
    }

    internal fun stopServiceDiscovery() {
        onServiceConnectedListener = null
        nsdManager.stopServiceDiscovery(nsdDiscoveryListener)
    }

    internal fun appendNewAddress(address: String, port: Int) {
        //TODO port should be set automatically
        configurationRepository.appendChildrenList(
                ChildData(address, port)
        )
    }

    internal interface OnServiceConnectedListener {
        fun onServiceConnected()

        fun onServiceConnectionError()
    }

    companion object {
        private const val SERVICE_NAME = "Baby Monitor Service"
        private const val SERVICE_TYPE = "_http._tcp."
    }
}
