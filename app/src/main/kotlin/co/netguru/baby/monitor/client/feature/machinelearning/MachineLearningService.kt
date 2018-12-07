package co.netguru.baby.monitor.client.feature.machinelearning

import android.annotation.TargetApi
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.support.v4.app.NotificationCompat
import co.netguru.baby.monitor.client.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MachineLearningService : IntentService("Machine Learning") {

    private val compositeDisposable = CompositeDisposable()
    private val aacRecorder by lazy { AacRecorder() }
    private val machineLearning by lazy { MachineLearning(applicationContext) }
    private var onCryingBabyDetected: () -> Unit = {}

    override fun onBind(intent: Intent?) = MainBinder()

    override fun onHandleIntent(intent: Intent?) {
        val notifyManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(notifyManager)
        startForeground(12, createNotification())
        aacRecorder.initRecorder()
                ?.observeOn(Schedulers.io())
                ?.subscribeBy(
                        onNext = this::handleRecordingData,
                        onError = Timber::e
                )?.addTo(compositeDisposable)
    }

    @TargetApi(26)
    private fun createChannel(notificationManager: NotificationManager) {
        val name = "Machine Learning Analyze"
        val description = "Notification for machine learning sound processing"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(name, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        notificationManager.createNotificationChannel(channel)
    }


    private fun createNotification(): Notification {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.logo else R.mipmap.ic_launcher

        return NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
                .setOngoing(true)
                .setSmallIcon(drawableResId)
                .setContentTitle("Machine Learning run")
                .setContentText("processing audio")
                .setTicker("processing")
                .build()
    }

    private fun handleRecordingData(array: ByteArray) {
        machineLearning.feedData(array)
                ?.subscribeOn(Schedulers.io())
                ?.subscribeBy(
                        onSuccess = this::handleMachineLearningData
                )?.addTo(compositeDisposable)
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
