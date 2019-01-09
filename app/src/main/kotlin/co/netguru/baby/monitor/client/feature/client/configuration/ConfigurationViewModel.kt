package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.data.ChildData
import co.netguru.baby.monitor.client.data.ChildRepository
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import dagger.Reusable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

@Reusable
class ConfigurationViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val childRepository: ChildRepository,
        private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    internal val serviceInfoData = Transformations.map(nsdServiceManager.serviceInfoData) {
        it ?: return@map null
        return@map it[0]
    }

    private val compositeDisposable = CompositeDisposable()

    internal fun appendNewAddress(
            address: String, port: Int, onSuccess: (Boolean) -> Unit
    ) {
        childRepository.appendChildrenList(
                ChildData("ws://$address:$port")
        ).subscribeBy(onSuccess = onSuccess).addTo(compositeDisposable)
    }

    internal fun discoverNsdService(onServiceConnectedListener: NsdServiceManager.OnServiceConnectedListener) {
        nsdServiceManager.discoverService(onServiceConnectedListener)
    }

    internal fun stopNsdServiceDiscovery() {
        nsdServiceManager.stopServiceDiscovery()
    }

    fun uploadAllRecordingsToFirebaseStorage() {
        firebaseRepository.uploadAllRecordingsToFirebaseStorage()
    }

    override fun onCleared() {
        super.onCleared()
        nsdServiceManager.stopServiceDiscovery()
        compositeDisposable.dispose()
    }
}
