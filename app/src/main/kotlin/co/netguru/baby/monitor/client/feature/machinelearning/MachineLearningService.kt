package co.netguru.baby.monitor.client.feature.machinelearning

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.support.v4.app.NotificationCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import kotlin.random.Random

class MachineLearningService : IntentService("MachineLearningService") {

    private val compositeDisposable = CompositeDisposable()
    private var aacRecorder: AacRecorder? = null
    private val machineLearning by lazy { MachineLearning(applicationContext) }
    private var onCryingBabyDetected: () -> Unit = {}

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHandler.createNotificationChannel(applicationContext)
        }
        startForeground(Random.nextInt(), createNotification())
        startRecording()
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
                .subscribeBy(
                        onSuccess = { map -> handleMachineLearningData(map, dataPair.first) }
                ).addTo(compositeDisposable)
    }

    private fun handleMachineLearningData(map: Map<String, Float>, rawData: ByteArray) {
        val entry = map.maxBy { it.value }
        if (entry?.key == MachineLearning.OUTPUT_2_CRYING_BABY) {
            Timber.i("Cry detected with probability of: ${entry.value}")
            onCryingBabyDetected()
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

    inner class MachineLearningBinder : Binder() {
        fun setOnCryingBabyDetectedListener(listener: () -> Unit) {
            onCryingBabyDetected = listener
        }

        fun startRecording() {
            this@MachineLearningService.startRecording()
        }

        fun stopRecording() {
            aacRecorder?.release()
            aacRecorder = null
        }

        fun cleanup() {
            compositeDisposable.dispose()
            onCryingBabyDetected = {}
            aacRecorder?.release()
            aacRecorder = null
        }
    }
}
