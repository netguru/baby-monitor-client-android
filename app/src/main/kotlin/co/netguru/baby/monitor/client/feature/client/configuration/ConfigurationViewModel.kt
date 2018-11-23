package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.data.server.NsdServiceManager
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val configurationRepository: ConfigurationRepository
) : ViewModel() {
    internal val serviceInfoData = nsdServiceManager.serviceInfoData
    private val compositeDisposable = CompositeDisposable()

    internal fun appendNewAddress(
            address: String, port: Int, onSuccess: (Boolean) -> Unit
    ) {
        //TODO change default name from address 25.10.2018
        configurationRepository.appendChildrenList(
                ChildData("ws://$address:$port", name = address)
        ).subscribeBy(onSuccess = onSuccess).addTo(compositeDisposable)
    }

    internal fun clearChildsData() {
        configurationRepository.childrenList = emptyList()
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
