package co.netguru.baby.monitor.client.feature.client.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.data.client.home.log.LogData
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.babycrynotification.SnoozeNotificationUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val sendFirebaseTokenUseCase: SendFirebaseTokenUseCase,
    private val sendBabyNameUseCase: SendBabyNameUseCase,
    private val snoozeNotificationUseCase: SnoozeNotificationUseCase,
    internal val rxWebSocketClient: RxWebSocketClient
) : ViewModel() {

    private val openSocketDisposables = CompositeDisposable()
    internal val logData = MutableLiveData<List<LogData>>()
    internal val selectedChild = dataRepository.getChildLiveData()
    internal val selectedChildAvailability = MutableLiveData<Boolean>()
    internal val toolbarState = MutableLiveData<ToolbarState>()
    internal val shouldDrawerBeOpen = MutableLiveData<Boolean>()
    internal val backButtonState = MutableLiveData<BackButtonState>()

    private val compositeDisposable = CompositeDisposable()

    fun fetchLogData() {
        dataRepository.getAllLogData()
            .subscribeOn(Schedulers.newThread())
            .subscribeBy(
                onNext = this::handleNextLogDataList,
                onError = Timber::e
            ).addTo(compositeDisposable)
    }

    fun saveConfiguration() {
        dataRepository.saveConfiguration(AppState.CLIENT)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = { Timber.i("state saved") },
                onError = Timber::e
            ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        rxWebSocketClient.dispose()
        compositeDisposable.dispose()
    }

    @RunsInBackground
    private fun handleNextLogDataList(list: List<LogDataEntity>) {
        logData.postValue(list.map { data ->
            data.toLogData(selectedChild.value?.image)
        })
    }

    fun setBackButtonState(backButtonState: BackButtonState) {
        this.backButtonState.postValue(backButtonState)
    }

    fun openSocketConnection(address: URI) {
        rxWebSocketClient.events(address)
            .subscribeBy(
                onNext = { event ->
                    Timber.i("Consuming event: $event.")
                    when (event) {
                        is RxWebSocketClient.Event.Open -> handleWebSocketOpen(rxWebSocketClient)
                        is RxWebSocketClient.Event.Close -> handleWebSocketClose()
                    }
                },
                onError = { error ->
                    Timber.i("Websocket error: $error.")
                }
            )
            .addTo(compositeDisposable)
    }

    private fun handleWebSocketOpen(client: RxWebSocketClient) {
        selectedChildAvailability.postValue(true)
        sendFirebaseTokenUseCase.sendFirebaseToken(client)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.d("Firebase token sent successfully.")
                }, onError = { error ->
                    Timber.w(error, "Error sending Firebase token.")
                })
            .addTo(openSocketDisposables)
        sendBabyNameUseCase.streamBabyName(client)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.d("Baby name sent successfully.")
                }, onError = { error ->
                    Timber.w(error, "Error sending baby name.")
                }
            )
            .addTo(openSocketDisposables)
    }

    private fun handleWebSocketClose() {
        selectedChildAvailability.postValue(false)
        openSocketDisposables.clear()
    }

    fun snoozeNotifications() {
        snoozeNotificationUseCase.snoozeNotifications()
            .addTo(compositeDisposable)
    }
}
