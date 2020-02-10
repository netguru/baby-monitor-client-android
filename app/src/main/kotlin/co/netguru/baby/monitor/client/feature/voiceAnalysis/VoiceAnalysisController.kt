package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.common.base.ServiceController
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.UserProperty
import co.netguru.baby.monitor.client.feature.babynotification.NotifyBabyEventUseCase
import co.netguru.baby.monitor.client.feature.debug.DebugModule
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector.Companion.MAX_NOISE_SENSITIVITY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class VoiceAnalysisController @Inject constructor(
    private val notifyBabyEventUseCase: NotifyBabyEventUseCase,
    private val sharedPrefsWrapper: FirebaseSharedPreferencesWrapper,
    private val notificationHandler: NotificationHandler,
    private val debugModule: DebugModule,
    private val noiseDetector: NoiseDetector,
    private val dataRepository: DataRepository,
    private val machineLearning: MachineLearning,
    private val analyticsManager: AnalyticsManager
) : ServiceController<VoiceAnalysisService> {

    private val compositeDisposable = CompositeDisposable()
    private var aacRecorder: AacRecorder? = null
    private var voiceAnalysisService: VoiceAnalysisService? = null
    var voiceAnalysisOption =
        VoiceAnalysisOption.MACHINE_LEARNING
        set(value) {
            field = value
            aacRecorder?.voiceAnalysisOption = value
        }
    var noiseSensitivity = NoiseDetector.DEFAULT_NOISE_SENSITIVITY

    override fun attachService(service: VoiceAnalysisService) {
        this.voiceAnalysisService = service
        notificationHandler.showForegroundNotification(service)
        dataRepository.getClientData()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onSuccess = {
                voiceAnalysisOption = it.voiceAnalysisOption
                noiseSensitivity = it.noiseSensitivity
                analyticsManager.setUserProperty(UserProperty.VoiceAnalysis(voiceAnalysisOption))
                startRecording()
            }, onComplete = this::startRecording)
            .addTo(compositeDisposable)
    }

    fun startRecording() {
        if (aacRecorder != null) return
        initBabyEventsSubscription()
        aacRecorder =
            AacRecorder(voiceAnalysisOption)
                .apply {
                    startRecording()
                        .subscribeOn(Schedulers.io())
                        .subscribeBy(
                            onComplete = { Timber.i("Recording completed") },
                            onError = Timber::e
                        ).addTo(compositeDisposable)
                    machineLearningData
                        .filter { voiceAnalysisOption == VoiceAnalysisOption.MACHINE_LEARNING }
                        .subscribeOn(Schedulers.newThread())
                        .subscribeBy(
                            onNext = this@VoiceAnalysisController::handleRecordingData,
                            onComplete = { Timber.i("Complete") },
                            onError = Timber::e
                        ).addTo(compositeDisposable)
                    soundDetectionData
                        .filter { voiceAnalysisOption == VoiceAnalysisOption.NOISE_DETECTION }
                        .subscribeOn(Schedulers.newThread())
                        .subscribeBy(
                            onNext = this@VoiceAnalysisController::handleRecordingData,
                            onComplete = { Timber.i("Complete") },
                            onError = Timber::e
                        ).addTo(compositeDisposable)
                }
    }

    private fun initBabyEventsSubscription() {
        notifyBabyEventUseCase
            .babyEvents()
            .subscribe()
            .addTo(compositeDisposable)
    }

    fun stopRecording() {
        Timber.i("Stop recording")
        aacRecorder?.release()
        aacRecorder = null
        compositeDisposable.clear()
    }

    private fun handleRecordingData(dataPair: Pair<ByteArray, ShortArray>) {
        machineLearning.processData(dataPair.second)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { map -> handleMachineLearningData(map, dataPair.first) },
                onError = { error -> voiceAnalysisService?.complain("ML model error", error) }
            ).addTo(compositeDisposable)
    }

    private fun handleRecordingData(noiseRecordingData: ShortArray) {
        noiseDetector.processData(noiseRecordingData, noiseRecordingData.size)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { decibels -> handleNoiseDetectorData(decibels) },
                onError = { error -> voiceAnalysisService?.complain("Noise detector error", error) }
            ).addTo(compositeDisposable)
    }

    private fun handleNoiseDetectorData(decibels: Double) {
        debugModule.sendSoundEvent(decibels.toInt())
        if (isNoiseDetected(decibels)) notifyBabyEventUseCase.notifyNoiseDetected()
        Timber.i("Decibels: $decibels")
    }

    private fun isNoiseDetected(decibels: Double) = noiseSensitivity > 0 &&
            decibels > -noiseSensitivity + MAX_NOISE_SENSITIVITY

    private fun handleMachineLearningData(map: Map<String, Float>, rawData: ByteArray) {
        val cryingProbability = map.getValue(MachineLearning.OUTPUT_2_CRYING_BABY)
        debugModule.sendCryingProbabilityEvent(cryingProbability)
        if (cryingProbability >= MachineLearning.CRYING_THRESHOLD) {
            Timber.i("Cry detected with probability of $cryingProbability.")
            notifyBabyEventUseCase.notifyBabyCrying()

            // Save baby recordings for later upload only when we have a strict user's permission
            if (sharedPrefsWrapper.isUploadEnablad()) {
                voiceAnalysisService?.run {
                    saveDataToFile(rawData)
                        .subscribeOn(Schedulers.io())
                        .subscribeBy(
                            onSuccess = { succeed -> Timber.i("File saved $succeed") },
                            onError = Timber::e
                        ).addTo(compositeDisposable)
                }
            }
        }
    }

    private fun cleanup() {
        compositeDisposable.dispose()
        aacRecorder?.release()
        aacRecorder = null
    }

    override fun detachService() {
        voiceAnalysisService?.stopForeground(true)
        voiceAnalysisService = null
        cleanup()
    }
}
