package co.netguru.baby.monitor.client.feature.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageSender
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
    private val resetAppUseCase: ResetAppUseCase,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableResetState = MutableLiveData<ResetState>()
    val resetState: LiveData<ResetState> = mutableResetState

    fun resetApp(messageSender: MessageSender? = null) {
        resetAppUseCase.resetApp(messageSender)
            .doOnSubscribe { mutableResetState.postValue(ResetState.InProgress) }
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    mutableResetState.postValue(ResetState.Completed)
                },
                onError = {
                    mutableResetState.postValue(ResetState.Failed)
                    Timber.w(it)
                }
            ).addTo(compositeDisposable)
    }

    internal fun isUploadEnabled() = firebaseRepository.isUploadEnablad()

    internal fun setUploadEnabled(enabled: Boolean) {
        firebaseRepository.setUploadEnabled(enabled)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
