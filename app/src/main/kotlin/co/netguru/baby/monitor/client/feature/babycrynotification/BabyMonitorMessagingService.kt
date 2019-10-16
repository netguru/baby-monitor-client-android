package co.netguru.baby.monitor.client.feature.babycrynotification

import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
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
        message.notification?.let(::handleRemoteNotification)
    }

    private fun handleRemoteNotification(remoteNotification: RemoteMessage.Notification) {
        val title = remoteNotification.title.orEmpty()
        val body = remoteNotification.body.orEmpty()
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.white_logo else R.mipmap.ic_launcher
        NotificationHandler.createNotificationChannel(this)

        NotificationManagerCompat.from(this).notify(
            0,
            notificationHandler.createNotification(title = title, content = body, iconResId = drawableResId)
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
}
