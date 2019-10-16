package co.netguru.baby.monitor.client.feature.babycrynotification

import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender.Companion.NOTIFICATION_TEXT
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender.Companion.NOTIFICATION_TITLE
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class BabyMonitorMessagingService : FirebaseMessagingService() {

    private val notificationHandler by lazy { NotificationHandler(this) }
    @Inject
    internal lateinit var database: DataRepository
    private val disposables = CompositeDisposable()

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Timber.i("Firebase token received $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Received a message: $message.")
        database.getChildData()
            .subscribeBy(
                onSuccess = { childDataEntity ->
                    if (isFiveMinutesSnoozeEnabled(childDataEntity)) {
                        Timber.i("Snooozed notification")
                    } else {
                        handleRemoteNotification(message.data)
                    }
                }
            )
    }

    private fun isFiveMinutesSnoozeEnabled(childDataEntity: ChildDataEntity) =
        System.currentTimeMillis() < childDataEntity.snoozeTimeStamp ?: 0

    private fun handleRemoteNotification(data: Map<String, String>) {
        val title = data[NOTIFICATION_TITLE].orEmpty()
        val text = data[NOTIFICATION_TEXT].orEmpty()
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            R.drawable.white_logo
        } else {
            R.mipmap.ic_launcher
        }
        NotificationHandler.createNotificationChannel(this)

        NotificationManagerCompat.from(this).notify(
            CRYING_NOTIFICATION_ID,
            notificationHandler.createNotification(
                title = title,
                content = text,
                iconResId = drawableResId
            )
        )

        database.insertLogToDatabase(LogDataEntity(title))
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.i("Log inserted into the database.")
                }, onError = { error ->
                    Timber.w(error, "Couldn't insert log into the database.")
                }
            )
            .addTo(disposables)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    companion object {
        const val CRYING_NOTIFICATION_ID = 321
    }
}
