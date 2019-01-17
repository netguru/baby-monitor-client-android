package co.netguru.baby.monitor.client.feature.client.configuration

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Intent
import androidx.navigation.NavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.splash.EnterActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val dataRepository: DataRepository,
        private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    internal val childList = MutableLiveData<List<ChildDataEntity>>()
    internal val serviceInfoData = Transformations.map(nsdServiceManager.serviceInfoData) {
        it ?: return@map null
        return@map it[0]
    }

    private val compositeDisposable = CompositeDisposable()

    init {
        dataRepository.getChildData()
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onNext = { list ->
                            childList.postValue(list)
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    internal fun appendNewAddress(
            address: String,
            port: Int,
            navController: NavController
    ) {
        dataRepository.addChildData(
                ChildDataEntity("ws://$address:$port")
        ).subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            navController.navigate(R.id.actionConfigurationConnectingDone)
                        },
                        onError = { e ->
                            Timber.e(e)
                        }
                ).addTo(compositeDisposable)
    }

    internal fun clearData(activity: Activity) {
        dataRepository.deleteAllData()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            handleDataCleared(activity)
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)

    }

    internal fun discoverNsdService(onServiceConnectedListener: NsdServiceManager.OnServiceConnectedListener) {
        nsdServiceManager.discoverService(onServiceConnectedListener)
    }

    internal fun stopNsdServiceDiscovery() {
        nsdServiceManager.stopServiceDiscovery()
    }

    internal fun uploadAllRecordingsToFirebaseStorage() {
        firebaseRepository.uploadAllRecordingsToFirebaseStorage()
    }

    override fun onCleared() {
        nsdServiceManager.stopServiceDiscovery()
        compositeDisposable.dispose()
        super.onCleared()
    }

    @RunsInBackground
    private fun handleDataCleared(activity: Activity) {
        activity.startActivity(
                Intent(activity, EnterActivity::class.java)
        )
        activity.finish()
    }
}
