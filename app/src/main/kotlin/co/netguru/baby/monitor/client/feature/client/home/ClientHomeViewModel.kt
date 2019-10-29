package co.netguru.baby.monitor.client.feature.client.home

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.data.client.home.log.LogData
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.babycrynotification.SnoozeNotificationUseCase
import co.netguru.baby.monitor.client.feature.communication.internet.CheckInternetConnectionUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.android.schedulers.AndroidSchedulers
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
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val restartAppUseCase: RestartAppUseCase,
    internal val rxWebSocketClient: RxWebSocketClient
) : ViewModel() {

    private val openSocketDisposables = CompositeDisposable()
    internal val logData = MutableLiveData<List<LogData>>()
    internal val selectedChild = dataRepository.getChildLiveData()
    private val mutableSelectedChildAvailability: MutableLiveData<Boolean> = MutableLiveData()
    internal val selectedChildAvailability =
        Transformations.distinctUntilChanged(mutableSelectedChildAvailability)
    internal val toolbarState = MutableLiveData<ToolbarState>()
    internal val shouldDrawerBeOpen = MutableLiveData<Boolean>()
    internal val backButtonState = MutableLiveData<BackButtonState>()
    private val mutableInternetConnectionAvailability = MutableLiveData<Boolean>()
    internal val internetConnectionAvailability: LiveData<Boolean> =
        mutableInternetConnectionAvailability
    private val compositeDisposable = CompositeDisposable()

    fun checkInternetConnection() {
        checkInternetConnectionUseCase.hasInternetConnection()
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { isConnected ->
                    mutableInternetConnectionAvailability.postValue(isConnected)
                },
                onError = { Timber.e(it) }
            )
            .addTo(compositeDisposable)
    }

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
        mutableSelectedChildAvailability.postValue(rxWebSocketClient.isOpen())
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
        mutableSelectedChildAvailability.postValue(true)
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
        mutableSelectedChildAvailability.postValue(false)
        openSocketDisposables.clear()
    }

    fun snoozeNotifications() {
        snoozeNotificationUseCase.snoozeNotifications()
            .addTo(compositeDisposable)
    }

    fun restartApp(activity: AppCompatActivity) {
        restartAppUseCase.restartApp(activity)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)
    }
}
