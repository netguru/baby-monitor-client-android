package co.netguru.baby.monitor.client.feature.babynotification

import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender.Companion.NOTIFICATION_TEXT
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender.Companion.NOTIFICATION_TITLE
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender.Companion.NOTIFICATION_TYPE
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.i("Firebase token received $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.i("Received a message: $message.")
        val notificationType = message.data[NOTIFICATION_TYPE] ?: NotificationType.DEFAULT.name
        when (NotificationType.valueOf(notificationType)) {
            NotificationType.CRY_NOTIFICATION, NotificationType.NOISE_NOTIFICATION -> handleBabyEvent(
                message.data
            )
            NotificationType.LOW_BATTERY_NOTIFICATION -> handleLowBatteryNotification(message.data)
            else -> {
                message.notification?.let {
                    handleRemoteNotification(
                        it.title.orEmpty(),
                        it.body.orEmpty()
                    )
                }
            }
        }
    }

    private fun handleLowBatteryNotification(data: MutableMap<String, String>) {
        handleRemoteNotification(
            data[NOTIFICATION_TITLE].orEmpty(),
            data[NOTIFICATION_TEXT].orEmpty()
        )
    }

    private fun handleBabyEvent(data: MutableMap<String, String>) {
        disposables += database.getChildData()
            .subscribeBy(
                onSuccess = { childDataEntity ->
                    if (isFiveMinutesSnoozeEnabled(childDataEntity)) {
                        Timber.i("Snooozed notification")
                    } else {
                        val actions = listOf(
                            NotificationHandler.createNotificationAction(
                                NotificationHandler.SHOW_CAMERA_ACTION,
                                this
                            ),
                            NotificationHandler.createNotificationAction(
                                NotificationHandler.SNOOZE_ACTION,
                                this
                            )
                        )
                        handleRemoteNotification(
                            data[NOTIFICATION_TITLE].orEmpty(),
                            data[NOTIFICATION_TEXT].orEmpty(),
                            actions
                        )
                    }
                }
            )
    }

    private fun isFiveMinutesSnoozeEnabled(childDataEntity: ChildDataEntity) =
        System.currentTimeMillis() < childDataEntity.snoozeTimeStamp ?: 0

    private fun handleRemoteNotification(
        title: String,
        text: String,
        actions: List<NotificationCompat.Action>? = null
    ) {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            R.drawable.ic_baby
        } else {
            R.mipmap.ic_launcher
        }
        NotificationHandler.createNotificationChannel(this)

        NotificationManagerCompat.from(this).notify(
            CRYING_NOTIFICATION_ID,
            notificationHandler.createNotification(
                title = title,
                content = text,
                iconResId = drawableResId,
                actions = actions
            )
        )

        logToDatabase(title)
    }

    private fun logToDatabase(title: String) {
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
