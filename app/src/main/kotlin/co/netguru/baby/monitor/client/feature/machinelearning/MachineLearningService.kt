package co.netguru.baby.monitor.client.feature.machinelearning

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.support.v4.app.NotificationCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.NotificationHandler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import kotlin.random.Random

class MachineLearningService : IntentService("Machine Learning Service") {

    private val compositeDisposable = CompositeDisposable()
    private val aacRecorder by lazy { AacRecorder() }
    private val machineLearning by lazy { MachineLearning(applicationContext) }
    private var onCryingBabyDetected: () -> Unit = {}

    override fun onBind(intent: Intent?) = MainBinder()

    override fun onHandleIntent(intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationHandler.createNotificationChannel(applicationContext)
        startForeground(Random.nextInt(), createNotification())
        aacRecorder.startRecording()
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onError = Timber::e
                ).addTo(compositeDisposable)
        aacRecorder.data
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onNext = this::handleRecordingData,
                        onComplete = { Timber.i("Complete") },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    private fun createNotification(): Notification {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.logo else R.mipmap.ic_launcher

        return NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
                .setOngoing(true)
                .setSmallIcon(drawableResId)
                .setProgress(0, 100, true)
                .setContentTitle(getString(R.string.notification_title_sound_processing))
                .build()
    }

    private fun handleRecordingData(array: ShortArray) {
        Timber.i("onNext data ready")
        machineLearning.processData(array)
                .subscribeOn(Schedulers.computation())
                .subscribeBy(
                        onSuccess = this::handleMachineLearningData
                ).addTo(compositeDisposable)
    }

    private fun handleMachineLearningData(map: Map<String, Float>) {
        val entry = map.maxBy { it.value }
        if (entry?.key == MachineLearning.OUTPUT_3_CRYING_BABY) {
            onCryingBabyDetected()
        }
    }

    inner class MainBinder : Binder() {
        fun setOnCryingBabyDetectedListener(listener: () -> Unit) {
            onCryingBabyDetected = listener
        }

        fun cleanup() {
            compositeDisposable.dispose()
            onCryingBabyDetected = {}
            aacRecorder.release()
        }
    }
}
