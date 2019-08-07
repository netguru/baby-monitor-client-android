package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ServerViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val dataRepository: DataRepository
) : ViewModel() {

    internal val shouldDrawerBeOpen = MutableLiveData<Boolean>()
    private val previewingVideo = MutableLiveData<Boolean>()
    private val timer = MutableLiveData<Long>()
    private var timerDisposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()

    internal fun previewingVideo(): LiveData<Boolean> =
            previewingVideo

    internal fun timer(): LiveData<Long> =
            timer

    internal fun resetTimer() {
        timerDisposable?.dispose()
        val totalTime = 65L
        Observable.intervalRange(0, totalTime, 0, 1, TimeUnit.SECONDS)
                .subscribeBy(
                        onNext = { elapsedSeconds ->
                            val secondsLeft = totalTime - elapsedSeconds
                            timer.postValue(secondsLeft)
                        },
                        onComplete = {
                            previewingVideo.postValue(false)
                        }
                )
                .let(::timerDisposable::set)
    }

    internal fun registerNsdService(
            onRegistrationFailed: (errorCode: Int) -> Unit
    ) {
        nsdServiceManager.registerService(object : NsdServiceManager.OnServiceConnectedListener {
            override fun onServiceConnectionError(errorCode: Int) {
                Timber.e("Service connection error with error code: $errorCode")
            }
            override fun onStartDiscoveryFailed(errorCode: Int) {
                Timber.e("Service registration failed with error code: $errorCode")
            }

            override fun onRegistrationFailed(errorCode: Int) {
                onRegistrationFailed(errorCode)
            }
        })
    }

    internal fun unregisterNsdService() {
        nsdServiceManager.unregisterService()
    }

    internal fun saveConfiguration() {
        dataRepository.saveConfiguration(AppState.SERVER)
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = { Timber.i("state saved") },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
