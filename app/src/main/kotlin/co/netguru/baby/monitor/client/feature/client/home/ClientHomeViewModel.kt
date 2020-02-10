package co.netguru.baby.monitor.client.feature.client.home

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.common.SingleLiveEvent
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.data.client.home.log.LogData
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.babynotification.SnoozeNotificationUseCase
import co.netguru.baby.monitor.client.feature.communication.internet.CheckInternetConnectionUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageParser
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val sendBabyNameUseCase: SendBabyNameUseCase,
    private val snoozeNotificationUseCase: SnoozeNotificationUseCase,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val restartAppUseCase: RestartAppUseCase,
    internal val rxWebSocketClient: RxWebSocketClient,
    private val voiceAnalysisUseCase: VoiceAnalysisUseCase,
    private val messageParser: MessageParser
) : ViewModel(), MessageController {

    private var connectionDisposable: Disposable? = null
    private val openSocketDisposables = CompositeDisposable()
    internal val logData = MutableLiveData<List<LogData>>()

    internal val selectedChildLiveData = dataRepository.getChildLiveData()

    internal val toolbarState = MutableLiveData<ToolbarState>()
    internal val shouldDrawerBeOpen = MutableLiveData<Boolean>()
    internal val backButtonState = MutableLiveData<BackButtonState>()

    private val mutableSelectedChildAvailability: MutableLiveData<Boolean> = MutableLiveData()
    internal val selectedChildAvailability =
        Transformations.distinctUntilChanged(mutableSelectedChildAvailability)

    private val mutableInternetConnectionAvailability = MutableLiveData<Boolean>()
    internal val internetConnectionAvailability: LiveData<Boolean> =
        mutableInternetConnectionAvailability

    internal val webSocketAction = SingleLiveEvent<String>()
    internal val errorAction = SingleLiveEvent<Throwable>()

    private val disposables = CompositeDisposable()

    fun checkInternetConnection() {
        disposables += checkInternetConnectionUseCase.hasInternetConnection()
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { isConnected ->
                    mutableInternetConnectionAvailability.postValue(isConnected)
                },
                onError = { Timber.e(it) }
            )
    }

    fun fetchLogData() {
        disposables += dataRepository.getAllLogData()
            .subscribeOn(Schedulers.newThread())
            .subscribeBy(
                onNext = this::handleNextLogDataList,
                onError = Timber::e
            )
    }

    fun saveConfiguration() {
        disposables += dataRepository.saveConfiguration(AppState.CLIENT)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = { Timber.i("state saved") },
                onError = Timber::e
            )
    }

    override fun onCleared() {
        super.onCleared()
        rxWebSocketClient.dispose()
        disposables.dispose()
    }

    @RunsInBackground
    private fun handleNextLogDataList(list: List<LogDataEntity>) {
        logData.postValue(list.map { data ->
            data.toLogData(selectedChildLiveData.value?.image)
        })
    }

    fun setBackButtonState(backButtonState: BackButtonState) {
        this.backButtonState.postValue(backButtonState)
    }

    fun openSocketConnection(urifier: (address: String) -> URI) {
        connectionDisposable = dataRepository.getChildData()
            .flatMapObservable { rxWebSocketClient.events(urifier.invoke(it.address)) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { event ->
                    Timber.i("Consuming event: $event.")
                    when (event) {
                        is RxWebSocketClient.Event.Open, RxWebSocketClient.Event.Connected
                        -> handleWebSocketOpen(rxWebSocketClient)
                        is RxWebSocketClient.Event.Disconnected
                        -> mutableSelectedChildAvailability.postValue(false)
                        is RxWebSocketClient.Event.Close -> handleWebSocketClose()
                        is RxWebSocketClient.Event.Message -> handleMessage(
                            messageParser.parseWebSocketMessage(
                                event
                            )
                        )
                    }
                },
                onError = { error ->
                    Timber.i("Websocket error: $error.")
                }
            )
    }

    fun closeSocketConnection() {
        connectionDisposable?.dispose()
        rxWebSocketClient.dispose()
        openSocketDisposables.clear()
    }

    private fun handleMessage(message: Message?) {
        message?.action?.let {
            handleMessageAction(it)
        }
    }

    private fun handleMessageAction(action: String) {
        webSocketAction.postValue(action)
    }

    private fun handleWebSocketOpen(client: RxWebSocketClient) {
        mutableSelectedChildAvailability.postValue(true)
        openSocketDisposables += sendBabyNameUseCase.streamBabyName(client)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.d("Baby name sent successfully.")
                }, onError = { error ->
                    errorAction.postValue(error)
                }
            )
        openSocketDisposables += voiceAnalysisUseCase.sendInitialVoiceAnalysisOption(client)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.d("Voice analysis sent successfully.")
                }, onError = { error ->
                    errorAction.postValue(error)
                }
            )
    }

    private fun handleWebSocketClose() {
        mutableSelectedChildAvailability.postValue(false)
        openSocketDisposables.clear()
    }

    fun snoozeNotifications() {
        disposables += snoozeNotificationUseCase.snoozeNotifications()
    }

    fun restartApp(activity: AppCompatActivity) {
        disposables += restartAppUseCase.restartApp(activity)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    override fun sendMessage(message: Message) {
        disposables += rxWebSocketClient.send(message)
            .subscribeBy(onError = { Timber.e(it) })
    }

    override fun receivedMessages(): Observable<Message> {
        return rxWebSocketClient.events()
            ?.ofType(RxWebSocketClient.Event.Message::class.java)
            ?.flatMap { webSocketMessage ->
                val message = messageParser.parseWebSocketMessage(webSocketMessage)
                message?.let {
                    Observable.just(it)
                } ?: Observable.error(Throwable("Failed Initialisation"))
            } ?: Observable.error(Throwable("Failed Initialisation"))
    }
}
