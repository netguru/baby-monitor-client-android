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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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
    private val analyticsManager: AnalyticsManager,
    private val recordingController: RecordingController
) : ServiceController<VoiceAnalysisService> {

    private val disposables = CompositeDisposable()
    private var recordingDisposable: Disposable? = null
    private var voiceAnalysisService: VoiceAnalysisService? = null
    var voiceAnalysisOption =
        VoiceAnalysisOption.MACHINE_LEARNING
        set(value) {
            field = value
            recordingController.voiceAnalysisOption = value
        }
    var noiseLevel = NoiseDetector.DEFAULT_NOISE_LEVEL

    override fun attachService(service: VoiceAnalysisService) {
        this.voiceAnalysisService = service
        notificationHandler.showForegroundNotification(service)
        dataRepository.getClientData()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onSuccess = {
                voiceAnalysisOption = it.voiceAnalysisOption
                noiseLevel = it.noiseLevel
                analyticsManager.setUserProperty(UserProperty.VoiceAnalysis(voiceAnalysisOption))
                startRecording()
            }, onComplete = this::startRecording)
            .addTo(disposables)
    }

    fun startRecording() {
        if (recordingDisposable != null && recordingDisposable?.isDisposed == false) return
        initBabyEventsSubscription()
        recordingDisposable = recordingController.startRecording()
            .filter { it is RecordingData.MachineLearning || it is RecordingData.NoiseDetection }
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = {
                    when (it) {
                        is RecordingData.NoiseDetection -> handleRecordingData(it)
                        is RecordingData.MachineLearning -> handleRecordingData(it)
                    }
                },
                onError = Timber::e
            )
    }

    private fun initBabyEventsSubscription() {
        notifyBabyEventUseCase
            .babyEvents()
            .subscribe()
            .addTo(disposables)
    }

    fun stopRecording() {
        Timber.i("Stop recording")
        recordingDisposable?.dispose()
        disposables.clear()
    }

    private fun handleRecordingData(recordingData: RecordingData.MachineLearning) {
        machineLearning.processData(recordingData.shortArray)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { map ->
                    handleMachineLearningData(
                        map,
                        recordingData.byteArray
                    )
                },
                onError = { error -> voiceAnalysisService?.complain("ML model error", error) }
            ).addTo(disposables)
    }

    private fun handleRecordingData(recordingData: RecordingData.NoiseDetection) {
        noiseDetector.processData(recordingData.shortArray)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { decibels -> handleNoiseDetectorData(decibels) },
                onError = { error -> voiceAnalysisService?.complain("Noise detector error", error) }
            ).addTo(disposables)
    }

    private fun handleNoiseDetectorData(decibels: Int) {
        debugModule.sendSoundEvent(decibels)
        if (isNoiseDetected(decibels)) notifyBabyEventUseCase.notifyNoiseDetected()
    }

    private fun isNoiseDetected(decibels: Int) = decibels > noiseLevel

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
                        ).addTo(disposables)
                }
            }
        }
    }

    private fun cleanup() {
        disposables.dispose()
        recordingDisposable?.dispose()
    }

    override fun detachService() {
        voiceAnalysisService?.stopForeground(true)
        voiceAnalysisService = null
        cleanup()
    }
}
