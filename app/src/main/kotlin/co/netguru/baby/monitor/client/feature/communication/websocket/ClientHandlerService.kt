package co.netguru.baby.monitor.client.feature.communication.websocket

import android.app.IntentService
import android.app.Notification
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.support.v4.app.NotificationCompat
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.database.DataRepository
import co.netguru.baby.monitor.client.data.ChildData
import co.netguru.baby.monitor.client.data.ChildRepository
import co.netguru.baby.monitor.client.feature.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.common.data.SingleEvent
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

class ClientHandlerService : IntentService("ClientHandlerService"), ClientsHandler.ConnectionListener {

    private val webSocketClientHandler by lazy {
        ClientsHandler(this, notificationHandler, dataRepository)
    }
    private val compositeDisposable = CompositeDisposable()
    private val childConnectionStatus = MutableLiveData<SingleEvent<Pair<ChildData, ConnectionStatus>>>()

    @Inject
    lateinit var notificationHandler: NotificationHandler
    @Inject
    lateinit var childRepository: ChildRepository
    @Inject
    lateinit var dataRepository: DataRepository

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHandler.createNotificationChannel(applicationContext)
        }
        startForeground(Random.nextInt(), createNotification())

        childRepository.childList.observeForever(this::addAllChildren)
    }

    override fun onBind(intent: Intent?) = ChildServiceBinder()

    override fun onHandleIntent(intent: Intent?) = Unit

    override fun onConnectionStatusChange(client: CustomWebSocketClient) {
        Timber.i("${client.address} ${client.connectionStatus}")
        val foundChild = childRepository.childList.value
                ?.find { childData ->
                    childData.address == client.address
                } ?: return
        if (!client.wasRetrying && client.connectionStatus == ConnectionStatus.DISCONNECTED) {
            childConnectionStatus.postValue(
                    SingleEvent(foundChild to ConnectionStatus.DISCONNECTED)
            )
        }
        if (client.connectionStatus == ConnectionStatus.CONNECTED) {
            childConnectionStatus.postValue(
                    SingleEvent(foundChild to ConnectionStatus.CONNECTED)
            )
        }
    }

    private fun createNotification(): Notification {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.logo else R.mipmap.ic_launcher

        return NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
                .setOngoing(true)
                .setSmallIcon(drawableResId)
                .setProgress(0, 100, true)
                .setContentTitle(getString(R.string.child_monitoring))
                .build()
    }

    private fun addAllChildren(list: List<ChildData>?) {
        for (child in list ?: return) {
            webSocketClientHandler.addClient(child.address)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onComplete = { Timber.i("Client added ${child.address}") },
                            onError = Timber::e
                    ).addTo(compositeDisposable)
        }
    }

    override fun onDestroy() {
        webSocketClientHandler.onDestroy()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    inner class ChildServiceBinder : Binder() {

        fun getChildConnectionStatus() = childConnectionStatus

        fun refreshChildWebSocketConnection(address: String?) {
            webSocketClientHandler.reconnectClient(address ?: return)
        }

        fun getChildClient(address: String) =
                webSocketClientHandler.getClient(address)
    }
}
