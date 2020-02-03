package co.netguru.baby.monitor.client.feature.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
    private val resetAppUseCase: ResetAppUseCase,
    private val firebaseRepository: FirebaseRepository,
    private val voiceAnalysisUseCase: VoiceAnalysisUseCase
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableResetState = MutableLiveData<ChangeState>()
    val resetState: LiveData<ChangeState> = mutableResetState

    private val mutableVoiceAnalysisOptionState =
        MutableLiveData<Pair<ChangeState, VoiceAnalysisOption?>>()
    val voiceAnalysisOptionState: LiveData<Pair<ChangeState, VoiceAnalysisOption?>> =
        mutableVoiceAnalysisOptionState

    fun resetApp(messageController: MessageController? = null) {
        resetAppUseCase.resetApp(messageController)
            .doOnSubscribe { mutableResetState.postValue(ChangeState.InProgress) }
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    mutableResetState.postValue(ChangeState.Completed)
                },
                onError = {
                    mutableResetState.postValue(ChangeState.Failed)
                    Timber.w(it)
                }
            ).addTo(compositeDisposable)
    }

    fun chooseVoiceAnalysisOption(
        messageController: MessageController,
        voiceAnalysisOption: VoiceAnalysisOption
    ) {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            voiceAnalysisOption
        )
            .doOnSubscribe { mutableVoiceAnalysisOptionState.postValue(ChangeState.InProgress to null) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { success ->
                    mutableVoiceAnalysisOptionState.postValue(
                        if (success) ChangeState.Completed to voiceAnalysisOption
                        else {
                            val previousOption =
                                when (voiceAnalysisOption) {
                                    VoiceAnalysisOption.MachineLearning -> VoiceAnalysisOption.NoiseDetection
                                    VoiceAnalysisOption.NoiseDetection -> VoiceAnalysisOption.MachineLearning
                                }
                            ChangeState.Failed to previousOption
                        }
                    )
                },
                onError = { Timber.e(it) }
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
