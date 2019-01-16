package co.netguru.baby.monitor.client.feature.splash

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.splash.AppState
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashViewModel @Inject constructor(
        private val dataRepository: DataRepository
) : ViewModel() {
    internal val appState = MutableLiveData<AppState>()

    private val compositeDisposable = CompositeDisposable()

    internal fun getSavedState() {
        Single.timer(DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
                .zipWith(dataRepository.getSavedState())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = {
                            appState.postValue(it.second)
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    companion object {
        private const val DELAY_MILLISECONDS = 1000L
    }
}
