package co.netguru.baby.monitor.client.feature.communication.websocket

import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Binder
import android.os.Build
import co.netguru.baby.monitor.client.application.DataModule
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import dagger.android.AndroidInjection
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ClientHandlerService : LifecycleService(), ClientsHandler.ConnectionListener {

    private val webSocketClientHandler by lazy {
        ClientsHandler(babyNameObservable, this, notificationHandler, dataRepository)
    }
    private val compositeDisposable = CompositeDisposable()
    private val childConnectionStatus = MutableLiveData<Pair<ChildDataEntity, ConnectionStatus>>()

    @Inject
    lateinit var notificationHandler: NotificationHandler
    @Inject
    lateinit var dataRepository: DataRepository
    @Inject
    @field:DataModule.BabyName
    internal lateinit var babyNameObservable: Flowable<String>

    private var childList: List<ChildDataEntity>? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
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
        return when {
            client == null -> ConnectionStatus.DISCONNECTED
            (!client.wasRetrying && client.connectionStatus == ConnectionStatus.DISCONNECTED) -> ConnectionStatus.DISCONNECTED
            (client.connectionStatus == ConnectionStatus.CONNECTED) -> ConnectionStatus.CONNECTED
            else -> ConnectionStatus.DISCONNECTED
        }
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
            if (childConnectionStatus.value?.second == ConnectionStatus.DISCONNECTED) {
                stopSelf()
            }
        }

        fun disableNotification(){
            webSocketClientHandler.notificationsEnabled(false)
        }

        fun enableNotification(){
            webSocketClientHandler.notificationsEnabled(true)
        }
    }
}
