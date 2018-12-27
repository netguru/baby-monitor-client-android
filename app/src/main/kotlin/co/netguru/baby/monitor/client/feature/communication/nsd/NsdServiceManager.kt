package co.netguru.baby.monitor.client.feature.communication.nsd

import android.arch.lifecycle.MutableLiveData
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class NsdServiceManager @Inject constructor(
        private val nsdManager: NsdManager
) {
    internal val serviceInfoData = MutableLiveData<List<NsdServiceInfo>>()
    private val serviceInfoList = mutableListOf<NsdServiceInfo>()
    private var discoveryStatus = DiscoveryStatus.STOPPED

    private val nsdServiceListener = object : NsdManager.RegistrationListener {
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) =
                Timber.e("Baby Monitor Service unregistration failed")

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) = Unit

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) =
                Timber.e("Baby Monitor Service registration failed")

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) =
                Timber.d("Baby Monitor Service registered")
    }

    private val nsdDiscoveryListener = object : NsdManager.DiscoveryListener {
        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Timber.i("service found: ${serviceInfo.serviceName}")
            if (serviceInfo.serviceName.contains(SERVICE_NAME)) {
                nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                        Timber.e("Baby Monitor Service resolve failed")
                        onServiceConnectedListener?.onServiceConnectionError()
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        if (serviceInfoList.find { it.host.hostAddress == serviceInfo.host.hostAddress } == null) {
                            serviceInfoList.add(serviceInfo)
                            serviceInfoData.postValue(serviceInfoList)
                        }
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

    private var onServiceConnectedListener: OnServiceConnectedListener? = null

    internal fun registerService() {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            port = WebRtcService.SERVER_PORT
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdServiceListener)
    }

    internal fun unregisterService() = nsdManager.unregisterService(nsdServiceListener)

    internal fun discoverService(onServiceConnectedListener: OnServiceConnectedListener) {
        if (discoveryStatus == DiscoveryStatus.STOPPED) {
            this.onServiceConnectedListener = onServiceConnectedListener
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, nsdDiscoveryListener)
            discoveryStatus = DiscoveryStatus.STARTED
        }
    }

    internal fun stopServiceDiscovery() {
        if (discoveryStatus == DiscoveryStatus.STARTED) {
            onServiceConnectedListener = null
            nsdManager.stopServiceDiscovery(nsdDiscoveryListener)
            discoveryStatus = DiscoveryStatus.STOPPED
        }
    }

    internal interface OnServiceConnectedListener {
        fun onServiceConnectionError()
    }

    companion object {
        private const val SERVICE_NAME = "Baby Monitor Service"
        private const val SERVICE_TYPE = "_http._tcp."
    }
}
