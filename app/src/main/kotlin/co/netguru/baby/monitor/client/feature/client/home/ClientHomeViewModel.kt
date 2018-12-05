package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import co.netguru.baby.monitor.client.feature.common.DataBounder
import co.netguru.baby.monitor.client.feature.common.FileManager
import co.netguru.baby.monitor.client.feature.common.RunsInBackground
import co.netguru.baby.monitor.client.feature.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.common.extensions.toJson
import co.netguru.baby.monitor.client.feature.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.MainService
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcClient
import co.netguru.baby.monitor.client.feature.communication.websocket.*
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
        private val configurationRepository: ConfigurationRepository,
        private val webSocketClientsHandler: ClientsHandler,
        private val fileManager: FileManager
) : ViewModel() {

    internal val lullabyCommand = MutableLiveData<LullabyCommand>()
    internal val selectedChild = MutableLiveData<ChildData>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()
    internal val selectedChildAvailability = MutableLiveData<ConnectionStatus>()
    internal var currentCall: RtcClient? = null
    internal val childList = MutableLiveData<List<ChildData>>()
    private val compositeDisposable = CompositeDisposable()

    init {
        webSocketClientsHandler.addConnectionListener { client ->
            Timber.i("Connected to ${client.address}")
        }
    }

    //TODO change it for real data fetch
    val activities: LiveData<List<LogActivityData.LogData>> = Transformations.switchMap(selectedChild) { child ->
        child ?: return@switchMap MutableLiveData<List<LogActivityData.LogData>>()
        return@switchMap Transformations.map(LogActivityData.getSampleData()) { activitiesList ->
            return@map activitiesList.map { LogActivityData.LogData(it.action, it.timeStamp, child.image) }
        }
    }

    fun refreshChildrenList() = Completable.fromAction {
        val list = configurationRepository.childrenList.toMutableList()
        if (selectedChild.value == null && list.isNotEmpty()) {
            selectedChild.postValue(list.first())
        }
        establishConnections(list)
        childList.postValue(list)
    }.subscribeOn(Schedulers.io())
            .subscribe { Timber.d("complete") }
            .addTo(compositeDisposable)

    fun setSelectedChildWithAddress(address: String) = Completable.fromAction {
        configurationRepository.childrenList.find { it.address == address }?.also {
            selectedChild.postValue(it)
            refreshChildrenList()
        }
    }.subscribeOn(Schedulers.io())
            .subscribe { Timber.d("complete") }
            .addTo(compositeDisposable)

    fun updateChildName(name: String) = {
        selectedChild.value?.name = name
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }.toCompletable()
            .subscribeOn(Schedulers.io())
            .subscribe { Timber.d("complete") }
            .addTo(compositeDisposable)

    fun saveImage(cache: File?): LiveData<DataBounder<Boolean>> {
        fileManager.deleteFileIfExists(selectedChild.value?.image ?: "")
        return fileManager.saveFile(cache) { filePath ->
            updateChildImageSource(filePath)
        }.subscribeWithLiveData()
    }

    fun repeatLullaby() {
        lullabyCommand.value?.let { command ->
            manageLullabyPlayback(command.lullabyName, Action.REPEAT)
        }
    }

    fun stopPlayback() {
        lullabyCommand.value?.let { command ->
            manageLullabyPlayback(command.lullabyName, Action.STOP)
        }
    }

    fun switchPlayback() {
        lullabyCommand.value?.let { command ->
            if (command.action == Action.PLAY || command.action == Action.RESUME) {
                manageLullabyPlayback(command.lullabyName, Action.PAUSE)
            } else {
                manageLullabyPlayback(command.lullabyName, Action.RESUME)
            }
        }
    }

    fun hangUp() = currentCall?.hangUp()

    fun manageLullabyPlayback(name: String, action: Action) {
        getSelectedChildClient()?.sendMessage(LullabyCommand(name, action).toJson())
    }

    private fun updateChildImageSource(path: String) = {
        selectedChild.value?.image = path
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }.toCompletable()
            .subscribeOn(Schedulers.io())
            .subscribe { Timber.d("complete") }

    fun setRemoteRenderer(remoteRenderer: SurfaceViewRenderer) {
        currentCall?.remoteRenderer = remoteRenderer
    }

    fun startCall(
            binder: MainService.MainBinder,
            context: Context,
            listener: (state: CallState) -> Unit
    ) {
        val client = getSelectedChildClient() ?: return
        currentCall = binder.createClient(client)
        currentCall?.startCall(context, listener)?.subscribeOn(Schedulers.newThread())
                ?.subscribeBy(
                        onComplete = { Timber.i("completed") },
                        onError = Timber::e
                )?.addTo(compositeDisposable)
    }

    fun isBabyDataFilled(): Boolean {
        val child = selectedChild.value ?: return false
        return (child.image != null)
    }

    override fun onCleared() {
        super.onCleared()
        currentCall?.cleanup()
        webSocketClientsHandler.onDestroy()
        compositeDisposable.dispose()
    }

    @RunsInBackground
    private fun establishConnections(list: List<ChildData>) {
        for (data in list) {
            webSocketClientsHandler.addClient(data.address)
                    .subscribeOn(Schedulers.io())
                    .doOnError { it.toString() }
                    .subscribeBy(
                            onSuccess = { Timber.i(it) }
                    )
        }
    }

    private fun getSelectedChildClient() =
            webSocketClientsHandler.getClient(selectedChild.value?.address)
}
