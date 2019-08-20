package co.netguru.baby.monitor.client.feature.machinelearning

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.support.annotation.UiThread
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import co.netguru.baby.monitor.client.R
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
import kotlin.random.Random

class MachineLearningService : IntentService("MachineLearningService") {

    private val compositeDisposable = CompositeDisposable()
    private var aacRecorder: AacRecorder? = null
    private val machineLearning by lazy { MachineLearning(applicationContext) }

    @Inject
    internal lateinit var notifyBabyCryingUseCase: NotifyBabyCryingUseCase

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHandler.createNotificationChannel(applicationContext)
        }
        startForeground(Random.nextInt(), createNotification())
        startRecording()
        notifyBabyCryingUseCase.subscribe(
            title = getString(R.string.notification_baby_is_crying_title),
            text = getString(R.string.notification_baby_is_crying_content)
        )
    }

    override fun onBind(intent: Intent?) = MachineLearningBinder()

    override fun onHandleIntent(intent: Intent?) = Unit

    override fun onDestroy() {
        compositeDisposable.dispose()
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

    private fun createNotification(): Notification {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.top_monitoring_icon else R.mipmap.ic_launcher

        return NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
                .setOngoing(true)
                .setSmallIcon(drawableResId)
                .setContentTitle(getString(R.string.notification_foreground_content_title))
                .setContentText(getString(R.string.notification_foreground_content_text))
                .build()
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
        if (cryingProbability >= MachineLearning.CRYING_THRESHOLD) {
            Timber.i("Cry detected with probability of $cryingProbability.")
            notifyBabyCryingUseCase.notifyBabyCrying()
            saveDataToFile(rawData)
        }
    }

    private fun saveDataToFile(rawData: ByteArray) {
        WavFileGenerator.saveAudio(
                applicationContext,
                rawData,
                AacRecorder.BIT_RATE.toByte(),
                AacRecorder.CHANELS,
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
            this@MachineLearningService.startRecording()
        }

        fun stopRecording() {
            aacRecorder?.release()
            aacRecorder = null
        }

        fun cleanup() {
            compositeDisposable.dispose()
            aacRecorder?.release()
            aacRecorder = null
        }
    }
}
