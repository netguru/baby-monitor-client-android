package co.netguru.baby.monitor.client.feature.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.Randomiser
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.ConfirmationItem
import co.netguru.baby.monitor.client.feature.communication.ConfirmationUseCase
import co.netguru.baby.monitor.client.feature.communication.ConfirmationUseCase.Companion.NUMBERS_OF_DIGITS_IN_ID
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
    private val resetAppUseCase: ResetAppUseCase,
    private val firebaseRepository: FirebaseRepository,
    private val confirmationUseCase: ConfirmationUseCase,
    private val randomiser: Randomiser
) : ViewModel() {

    var noiseLevelInitialValue: Int = NoiseDetector.DEFAULT_NOISE_LEVEL
    private val disposables = CompositeDisposable()
    private val mutableResetState = MutableLiveData<ChangeState>()
    val resetState: LiveData<ChangeState> = mutableResetState

    private val mutableVoiceAnalysisOptionState =
        MutableLiveData<Pair<ChangeState, VoiceAnalysisOption?>>()
    val voiceAnalysisOptionState: LiveData<Pair<ChangeState, VoiceAnalysisOption?>> =
        mutableVoiceAnalysisOptionState

    private val mutableNoiseLevelState =
        MutableLiveData<Pair<ChangeState, Int?>>()
    val noiseLevelState: LiveData<Pair<ChangeState, Int?>> =
        mutableNoiseLevelState

    fun resetApp(messageController: MessageController? = null) {
        disposables += resetAppUseCase.resetApp(messageController)
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
            )
    }

    fun chooseVoiceAnalysisOption(
        messageController: MessageController,
        voiceAnalysisOption: VoiceAnalysisOption
    ) {
        disposables += confirmationUseCase.changeValue(
            messageController,
            getVoiceAnalysisConfirmationItem(voiceAnalysisOption)
        )
            .doOnSubscribe { mutableVoiceAnalysisOptionState
                .postValue(ChangeState.InProgress to null) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { success ->
                    mutableVoiceAnalysisOptionState.postValue(
                        if (success) {
                            ChangeState.Completed to voiceAnalysisOption
                        } else {
                            val previousOption =
                                when (voiceAnalysisOption) {
                                    VoiceAnalysisOption.MACHINE_LEARNING
                                    -> VoiceAnalysisOption.NOISE_DETECTION
                                    VoiceAnalysisOption.NOISE_DETECTION
                                    -> VoiceAnalysisOption.MACHINE_LEARNING
                                }
                            ChangeState.Failed to previousOption
                        }
                    )
                },
                onError = { Timber.e(it) }
            )
    }

    private fun getVoiceAnalysisConfirmationItem(voiceAnalysisOption: VoiceAnalysisOption)
            : ConfirmationItem<VoiceAnalysisOption> {
        return object : ConfirmationItem<VoiceAnalysisOption> {
            override fun onSuccessAction(dataRepository: DataRepository): Completable =
                dataRepository.updateVoiceAnalysisOption(voiceAnalysisOption)

            override val value: VoiceAnalysisOption = voiceAnalysisOption
            override val sentMessage = Message(
                voiceAnalysisOption = voiceAnalysisOption.name,
                confirmationId = randomiser.getRandomDigits(NUMBERS_OF_DIGITS_IN_ID)
                    .joinToString("")
            )
        }
    }

    internal fun isUploadEnabled() = firebaseRepository.isUploadEnabled()

    internal fun setUploadEnabled(enabled: Boolean) {
        firebaseRepository.setUploadEnabled(enabled)
    }

    fun changeNoiseLevel(messageController: MessageController, level: Int) {
        disposables += confirmationUseCase.changeValue(
            messageController,
            getNoiseLevelConfirmationItem(level)
        )
            .doOnSubscribe { mutableNoiseLevelState
                .postValue(ChangeState.InProgress to null) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { success ->
                    if (success) {
                        noiseLevelInitialValue = level
                        mutableNoiseLevelState
                            .postValue(ChangeState.Completed to level)
                    } else {
                        mutableNoiseLevelState
                            .postValue(ChangeState.Failed to noiseLevelInitialValue)
                    }
                },
                onError = { Timber.e(it) }
            )
    }

    private fun getNoiseLevelConfirmationItem(level: Int): ConfirmationItem<Int> {
        return object : ConfirmationItem<Int> {
            override fun onSuccessAction(dataRepository: DataRepository): Completable =
                dataRepository.updateNoiseLevel(level)

            override val value: Int = level
            override val sentMessage = Message(
                noiseLevel = level,
                confirmationId = randomiser.getRandomDigits(NUMBERS_OF_DIGITS_IN_ID)
                    .joinToString("")
            )
        }
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }
}
