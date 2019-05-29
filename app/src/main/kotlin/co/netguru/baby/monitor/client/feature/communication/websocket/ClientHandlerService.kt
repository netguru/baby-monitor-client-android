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
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.communication.SingleEvent
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

class ClientHandlerService : LifecycleService(), ClientsHandler.ConnectionListener {

    private val webSocketClientHandler by lazy {
        ClientsHandler(this, notificationHandler, dataRepository)
    }
    private val compositeDisposable = CompositeDisposable()
    private val childConnectionStatus = MutableLiveData<Pair<ChildDataEntity, ConnectionStatus>>()

    @Inject
    lateinit var notificationHandler: NotificationHandler
    @Inject
    lateinit var dataRepository: DataRepository

    private var childList: List<ChildDataEntity>? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHandler.createNotificationChannel(applicationContext)
        }
        startForeground(Random.nextInt(), createNotification())

        dataRepository.getChildData()
                .observe(this, Observer { list ->
                    addAllChildren(list ?: return@Observer)
                })
    }

    override fun onBind(intent: Intent?): Binder {
        super.onBind(intent)
        return ChildServiceBinder()
    }

    override fun onConnectionStatusChange(client: CustomWebSocketClient) {
        Timber.i("${client.address} ${client.connectionStatus}")
        val foundChild = childList
                ?.find { childData ->
                    childData.address == client.address
                } ?: return
        if (!client.wasRetrying && client.connectionStatus == ConnectionStatus.DISCONNECTED) {
            childConnectionStatus.postValue(
                    foundChild to ConnectionStatus.DISCONNECTED
            )
        }
        if (client.connectionStatus == ConnectionStatus.CONNECTED) {
            childConnectionStatus.postValue(
                    foundChild to ConnectionStatus.CONNECTED
            )
        }
    }

    fun getCurrentConnectionStatus(): ConnectionStatus {
        val client = webSocketClientHandler.getClient("")
        if (client != null) {

            if (!client.wasRetrying && client.connectionStatus == ConnectionStatus.DISCONNECTED) {
                return  ConnectionStatus.DISCONNECTED
            }
            if (client.connectionStatus == ConnectionStatus.CONNECTED) {
                return ConnectionStatus.CONNECTED
            }
        }
        return ConnectionStatus.UNKNOWN
    }

    private fun createNotification(): Notification {
        val drawableResId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            R.drawable.top_monitoring_icon else R.mipmap.ic_launcher

        return NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
                .setOngoing(true)
                .setSmallIcon(drawableResId)
                .setContentTitle(getString(R.string.notification_foreground_content_title))
                .setContentText(getString(R.string.notification_foreground_content_text))
                .setContentIntent(PendingIntent.getActivity(applicationContext,0,Intent(applicationContext,ClientHomeActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .build()
    }

    private fun addAllChildren(list: List<ChildDataEntity>?) {
        childList = list
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

        fun getConnectionStatus(): ConnectionStatus {
            return getCurrentConnectionStatus()
        }
        fun getChildConnectionStatusLivedata() = childConnectionStatus

        fun refreshChildWebSocketConnection(address: String?) {
            webSocketClientHandler.reconnectClient(address ?: return)
        }

        fun getChildClient(address: String) =
                webSocketClientHandler.getClient(address)

        fun stopService() {
            if(childConnectionStatus.value?.second == ConnectionStatus.DISCONNECTED){
                stopSelf()
            }
        }
    }
}
