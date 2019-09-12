package co.netguru.baby.monitor.client.feature.communication.websocket

import android.app.Notification
import android.app.PendingIntent
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.support.v4.app.NotificationCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.DataModule
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import dagger.android.AndroidInjection
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

class ClientHandlerService : LifecycleService() {

    private val compositeDisposable = CompositeDisposable()
    private val childConnectionStatus = MutableLiveData<Pair<ChildDataEntity, ConnectionStatus>>()

    @Inject
    lateinit var notificationHandler: NotificationHandler
    @Inject
    lateinit var dataRepository: DataRepository
    @Inject
    @field:DataModule.BabyName
    internal lateinit var babyNameObservable: Flowable<String>


    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHandler.createNotificationChannel(applicationContext)
        }
        startForeground(Random.nextInt(), createNotification())
    }

    override fun onBind(intent: Intent?): Binder {
        super.onBind(intent)
        return ChildServiceBinder()
    }

    private fun createNotification(): Notification {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.top_monitoring_icon else R.mipmap.ic_launcher

        return NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
                .setOngoing(true)
                .setSmallIcon(drawableResId)
                .setContentTitle(getString(R.string.notification_foreground_content_title))
                .setContentText(getString(R.string.notification_foreground_content_text))
                .setContentIntent(PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, ClientHomeActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .build()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    inner class ChildServiceBinder : Binder() {
        fun stopService() {
            if (childConnectionStatus.value?.second == ConnectionStatus.DISCONNECTED) {
                stopSelf()
            }
        }
    }
}
