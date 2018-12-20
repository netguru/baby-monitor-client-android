package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.data.ChildData
import co.netguru.baby.monitor.client.data.ChildRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val childRepository: ChildRepository
) : ViewModel() {

    internal val serviceInfoData = Transformations.map(nsdServiceManager.serviceInfoData) {
        it ?: return@map null
        return@map it[0]
    }

    private val compositeDisposable = CompositeDisposable()

    internal fun appendNewAddress(
            address: String, port: Int, onSuccess: (Boolean) -> Unit
    ) {
        //TODO change default name from address 25.10.2018
        childRepository.appendChildrenList(
                ChildData("ws://$address:$port", name = address)
        ).subscribeBy(onSuccess = onSuccess).addTo(compositeDisposable)
    }

    internal fun clearChildrenData() {
        childRepository.setChildData(emptyList())
    }

    internal fun discoverNsdService(onServiceConnectedListener: NsdServiceManager.OnServiceConnectedListener) {
        nsdServiceManager.discoverService(onServiceConnectedListener)
    }

    internal fun stopNsdServiceDiscovery() {
        nsdServiceManager.stopServiceDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
        nsdServiceManager.stopServiceDiscovery()
        compositeDisposable.dispose()
    }
}
