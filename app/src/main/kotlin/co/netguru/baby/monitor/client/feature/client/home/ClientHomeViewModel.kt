package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.common.extensions.toJson
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.feature.common.DataBounder
import co.netguru.baby.monitor.client.feature.websocket.Action
import co.netguru.baby.monitor.client.feature.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.websocket.CustomWebSocketClient
import co.netguru.baby.monitor.client.feature.websocket.LullabyCommand
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.SingleSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.single.SingleDefer
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
        private val configurationRepository: ConfigurationRepository
) : ViewModel() {

    internal val lullabyCommand = MutableLiveData<LullabyCommand>()
    internal val selectedChild = MutableLiveData<ChildData>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()
    internal val selectedChildAvailability = MutableLiveData<ConnectionStatus>()
    private val childList = MutableLiveData<DataBounder<List<ChildData>>>()
    private val compositeDisposable = CompositeDisposable()
    private var webSocketClient: CustomWebSocketClient? = null

    fun getChildrenList(): LiveData<DataBounder<List<ChildData>>> = SingleDefer.defer {
        SingleSource<List<ChildData>> {
            val list = configurationRepository.childrenList.toMutableList()
            if (selectedChild.value == null && list.isNotEmpty()) {
                selectedChild.postValue(list.first())
            }
            it.onSuccess(list)
        }
    }.subscribeOn(Schedulers.io()).subscribeWithLiveData(childList)

    fun setSelectedChildWithAddress(address: String) = Completable.fromAction {
        configurationRepository.childrenList.find { it.serverUrl == address }?.also {
            selectedChild.postValue(it)
            getChildrenList()
        }
    }.subscribeOn(Schedulers.io()).subscribe { Timber.d("complete") }

    fun updateChildName(name: String) = {
        selectedChild.value?.name = name
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }.toCompletable()
            .subscribeOn(Schedulers.io())
            .subscribe { Timber.d("complete") }

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

    fun connectToServer(childData: ChildData) {
        webSocketClient?.onDestroy()
        compositeDisposable.clear()
        webSocketClient = CustomWebSocketClient(
                childData.webSocketAddress,
                onAvailabilityChange = { availability ->
                    if (availability == ConnectionStatus.DISCONNECTED) {
                        tryToReconnect(childData)
                    }
                    selectedChildAvailability.postValue(availability)
                },
                onCommandResponse = { lullabyCommand.postValue(it) }
        )
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

    fun manageLullabyPlayback(name: String, action: Action) {
        webSocketClient?.sendMessage(LullabyCommand(name, action).toJson())
    }

    private fun tryToReconnect(childData: ChildData) {
        Observable
                .timer(5000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onNext = { connectToServer(childData) },
                        onError = { exception ->
                            Timber.e(exception)
                            webSocketClient?.onDestroy()
                        }
                ).addTo(compositeDisposable)
    }

    private fun updateChildImageSource(path: String) = {
        selectedChild.value?.image = path
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }.toCompletable()
            .subscribeOn(Schedulers.io())
            .subscribe { Timber.d("complete") }

    override fun onCleared() {
        super.onCleared()
        webSocketClient?.onDestroy()
        compositeDisposable.dispose()
    }
}
