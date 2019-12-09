package co.netguru.baby.monitor.client.feature.machinelearning

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import androidx.annotation.UiThread
import android.widget.Toast
import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.babycrynotification.NotifyBabyCryingUseCase
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import co.netguru.baby.monitor.client.feature.debug.DebugModule

class MachineLearningService : IntentService("MachineLearningService") {

    private val compositeDisposable = CompositeDisposable()
    private var aacRecorder: AacRecorder? = null
    private val machineLearning by lazy { MachineLearning(applicationContext) }

    @Inject
    internal lateinit var notifyBabyCryingUseCase: NotifyBabyCryingUseCase
    @Inject
    internal lateinit var sharedPrefsWrapper: FirebaseSharedPreferencesWrapper
    @Inject
    internal lateinit var notificationHandler: NotificationHandler
    @Inject
    internal lateinit var debugModule: DebugModule

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        notificationHandler.showForegroundNotification(this)
        startRecording()
    }

    override fun onBind(intent: Intent?) = MachineLearningBinder()

    override fun onHandleIntent(intent: Intent?) = Unit

    override fun onDestroy() {
        compositeDisposable.dispose()
        stopForeground(true)
        super.onDestroy()
    }

    private fun startRecording() {
        aacRecorder = AacRecorder()
        aacRecorder?.startRecording()
            ?.subscribeOn(Schedulers.computation())
            ?.subscribeBy(
                onComplete = { Timber.i("Recording completed") },
                onError = Timber::e
            )?.addTo(compositeDisposable)
        aacRecorder?.data
            ?.subscribeOn(Schedulers.newThread())
            ?.subscribeBy(
                onNext = this::handleRecordingData,
                onComplete = { Timber.i("Complete") },
                onError = Timber::e
            )?.addTo(compositeDisposable)
    }

    private fun handleRecordingData(dataPair: Pair<ByteArray, ShortArray>) {
        machineLearning.processData(dataPair.second)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { map -> handleMachineLearningData(map, dataPair.first) },
                onError = { error -> complain("ML model error", error) }
            ).addTo(compositeDisposable)
    }

    private fun handleMachineLearningData(map: Map<String, Float>, rawData: ByteArray) {
        val cryingProbability = map.getValue(MachineLearning.OUTPUT_2_CRYING_BABY)
        debugModule.sendCryingProbabilityEvent(cryingProbability)
        if (cryingProbability >= MachineLearning.CRYING_THRESHOLD) {
            Timber.i("Cry detected with probability of $cryingProbability.")
            notifyBabyCryingUseCase.notifyBabyCrying()

            // Save baby recordings for later upload only when we have a strict user's permission
            if (sharedPrefsWrapper.isUploadEnablad()) {
                saveDataToFile(rawData)
            }
        }
    }

    private fun saveDataToFile(rawData: ByteArray) {
        WavFileGenerator.saveAudio(
            applicationContext,
            rawData,
            AacRecorder.BIT_RATE.toByte(),
            AacRecorder.CHANNELS,
            AacRecorder.SAMPLING_RATE,
            AacRecorder.BIT_RATE
        ).subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { succeed -> Timber.i("File saved $succeed") },
                onError = Timber::e
            ).addTo(compositeDisposable)
    }

    @UiThread
    private fun complain(message: String, error: Throwable) {
        Timber.w(error, message)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    inner class MachineLearningBinder : Binder() {
        fun startRecording() {
            stopIfRunning()
            Timber.i("Start recording")
            this@MachineLearningService.startRecording()
        }

        private fun stopIfRunning() {
            // In case of multiple disconnected statuses from RtcServerConnectionState
            if (aacRecorder != null) stopRecording()
        }

        fun stopRecording() {
            Timber.i("Stop recording")
            aacRecorder?.release()
            aacRecorder = null
            compositeDisposable.clear()
        }

        fun cleanup() {
            compositeDisposable.dispose()
            aacRecorder?.release()
            aacRecorder = null
        }
    }
}
