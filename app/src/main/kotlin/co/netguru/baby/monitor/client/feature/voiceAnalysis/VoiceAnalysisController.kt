package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.common.base.ServiceController
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.babynotification.NotifyBabyEventUseCase
import co.netguru.baby.monitor.client.feature.debug.DebugModule
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector
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
    private val machineLearning: MachineLearning
) : ServiceController<VoiceAnalysisService> {

    private val compositeDisposable = CompositeDisposable()
    private var aacRecorder: AacRecorder? = null
    private var voiceAnalysisService: VoiceAnalysisService? = null
    var voiceAnalysisOption =
        VoiceAnalysisOption.MachineLearning
        set(value) {
            field = value
            aacRecorder?.voiceAnalysisOption = value
        }

    override fun attachService(service: VoiceAnalysisService) {
        this.voiceAnalysisService = service
        notificationHandler.showForegroundNotification(service)
        dataRepository.getClientData()
            .subscribeOn(Schedulers.io())
            .subscribeBy(onSuccess = {
                voiceAnalysisOption = it.voiceAnalysisOption
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
                        .filter { voiceAnalysisOption == VoiceAnalysisOption.MachineLearning }
                        .subscribeOn(Schedulers.newThread())
                        .subscribeBy(
                            onNext = this@VoiceAnalysisController::handleRecordingData,
                            onComplete = { Timber.i("Complete") },
                            onError = Timber::e
                        ).addTo(compositeDisposable)
                    soundDetectionData
                        .filter { voiceAnalysisOption == VoiceAnalysisOption.NoiseDetection }
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
        if (decibels > NoiseDetector.DEFAULT_NOISE_THRESHOLD) notifyBabyEventUseCase.notifyNoiseDetected()
        Timber.i("Decibels: $decibels")
    }

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

    fun cleanup() {
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
