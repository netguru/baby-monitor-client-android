package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.netguru.baby.monitor.client.data.communication.nsd.DiscoveryStatus
import co.netguru.baby.monitor.client.feature.communication.SERVER_PORT
import dagger.Reusable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@Reusable
class NsdServiceManager @Inject constructor(
    private val nsdManager: NsdManager,
    private val deviceNameProvider: IDeviceNameProvider
) {
    private val mutableNsdStateLiveData = MutableLiveData<NsdState>()
    internal val nsdStateLiveData: LiveData<NsdState> = mutableNsdStateLiveData
    private val serviceInfoList = mutableListOf<NsdServiceInfo>()
    private var discoveryStatus = DiscoveryStatus.STOPPED
    private val disposables = CompositeDisposable()

    private var nsdRegistrationListener: NsdManager.RegistrationListener? = null

    private fun createNsdRegistrationListener() = object : NsdManager.RegistrationListener {
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) =
            Timber.e("Baby Monitor Service unregistration failed")

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) =
            Timber.i("Baby Monitor service unregistered")

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Timber.e("Baby Monitor Service registration failed")
            mutableNsdStateLiveData.postValue(NsdState.Error(RegistrationFailedExcetpion()))
        }

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) =
            Timber.d("Baby Monitor Service registered")
    }

    private var nsdDiscoveryListener: NsdManager.DiscoveryListener? = null

    private var nsdDiscoveryObservable: Observable<NsdServiceInfo>? = null

    internal fun registerService() {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "${deviceNameProvider.getDeviceName()} $SERVICE_NAME"
            serviceType = SERVICE_TYPE
            port = SERVER_PORT
        }
        nsdRegistrationListener = createNsdRegistrationListener()
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener)
    }

    internal fun unregisterService() {
        nsdManager.unregisterService(nsdRegistrationListener)
        nsdRegistrationListener = null
    }

    internal fun discoverService() {
        if (discoveryStatus == DiscoveryStatus.STOPPED) {
            startServiceDiscovery()
        }
    }

    private fun startServiceDiscovery() {
        val nsdDiscoveryObservable: Observable<NsdServiceInfo> = Observable.create { emitter ->
            initDiscovery(emitter)
        }
        this.nsdDiscoveryObservable = nsdDiscoveryObservable

        nsdDiscoveryObservable.subscribeOn(Schedulers.computation())
            .concatMapSingle(this::createResolveServiceSingle)
            .subscribeBy(onNext = this::handleResolvedService,
                onError = {
                    handleNsdError(it)
                })
            .addTo(disposables)
        discoveryStatus = DiscoveryStatus.STARTED
    }

    private fun initDiscovery(emitter: ObservableEmitter<NsdServiceInfo>) {
        nsdDiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Timber.i("service found: ${serviceInfo.serviceName}")
                if (serviceInfo.serviceName.contains(SERVICE_NAME)) {
                    emitter.onNext(serviceInfo)
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) = Unit
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                emitter.onError(StartDiscoveryFailedException())
            }

            override fun onDiscoveryStarted(serviceType: String?) =
                Timber.d("Baby Monitor Service discovery started")

            override fun onDiscoveryStopped(serviceType: String?) {
                emitter.onComplete()
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) =
                Timber.e("Baby Monitor Service failed lost")
        }
        nsdManager.discoverServices(
            SERVICE_TYPE,
            PROTOCOL_DNS_SD,
            nsdDiscoveryListener
        )
        emitter.setCancellable {
            nsdManager.stopServiceDiscovery(nsdDiscoveryListener)
        }
    }

    private fun handleNsdError(throwable: Throwable) {
        mutableNsdStateLiveData.postValue(NsdState.Error(throwable))
        Timber.e(throwable)
    }

    private fun createResolveServiceSingle(notResolvedServiceInfo: NsdServiceInfo): Single<NsdServiceInfo> {
        return Single.create<NsdServiceInfo> { emitter ->
            nsdManager.resolveService(
                notResolvedServiceInfo,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(
                        serviceInfo: NsdServiceInfo?,
                        errorCode: Int
                    ) {
                        emitter.onError(ResolveFailedException())
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        emitter.onSuccess(serviceInfo)
                    }
                })
        }
    }

    private fun handleResolvedService(resolvedServiceInfo: NsdServiceInfo) {
        if (serviceInfoList.find { it.host.hostAddress == resolvedServiceInfo.host.hostAddress } == null) {
            serviceInfoList.add(resolvedServiceInfo)
            Timber.i("${resolvedServiceInfo.serviceName} service added")
            mutableNsdStateLiveData.postValue(
                NsdState.InProgress(
                    serviceInfoList
                )
            )
        }
    }

    internal fun stopServiceDiscovery() {
        if (discoveryStatus == DiscoveryStatus.STARTED) {
            disposables.clear()
            nsdDiscoveryObservable = null
            discoveryStatus = DiscoveryStatus.STOPPED
        }
    }

    companion object {
        const val SERVICE_NAME = "Baby Monitor Service"
        private const val SERVICE_TYPE = "_http._tcp."
        private const val PROTOCOL_DNS_SD = 0x0001
    }
}
