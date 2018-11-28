package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import co.netguru.baby.monitor.client.feature.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.common.extensions.toJson
import co.netguru.baby.monitor.client.feature.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.MainService
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcClient
import co.netguru.baby.monitor.client.feature.communication.websocket.Action
import co.netguru.baby.monitor.client.feature.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketClient
import co.netguru.baby.monitor.client.feature.communication.websocket.LullabyCommand
import io.reactivex.Completable
import io.reactivex.SingleSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.single.SingleDefer
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
        private val configurationRepository: ConfigurationRepository
) : ViewModel() {

    internal val lullabyCommand = MutableLiveData<LullabyCommand>()
    internal val selectedChild = MutableLiveData<ChildData>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()
    internal val selectedChildAvailability = MutableLiveData<ConnectionStatus>()
    internal var currentCall: RtcClient? = null
    internal val childList = MutableLiveData<List<ChildData>>()
    private val compositeDisposable = CompositeDisposable()
    private var webSocketClient: CustomWebSocketClient? = null

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

    fun saveImage(context: Context, cache: File?) = SingleDefer.defer {
        SingleSource<Boolean> {
            if (cache == null) {
                it.onError(FileNotFoundException())
                return@SingleSource
            }
            //TODO check photo orientation
            val file = File(context.filesDir, cache.name)
            try {
                cache.copyTo(file, true)
                val previousPhoto = File(selectedChild.value?.image ?: "")
                if (previousPhoto.exists()) {
                    previousPhoto.delete()
                }
                updateChildImageSource(file.absolutePath)
                it.onSuccess(true)
            } catch (e: IOException) {
                e.printStackTrace()
                it.onError(e)
            }
        }
    }.subscribeOn(Schedulers.io()).subscribeWithLiveData()

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
        webSocketClient?.sendMessage(LullabyCommand(name, action).toJson())
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
            address: String,
            context: Context,
            listener: (state: CallState) -> Unit
    ) {
        currentCall = binder.createClient(address)
        currentCall?.startCall(context, listener)?.subscribeOn(Schedulers.newThread())
                ?.subscribeBy(
                        onComplete = { Timber.i("completed") },
                        onError = Timber::e
                )?.addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        currentCall?.cleanup()
        webSocketClient?.onDestroy()
        compositeDisposable.dispose()
    }
}
