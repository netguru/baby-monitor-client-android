package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.data.ChildData
import co.netguru.baby.monitor.client.data.ChildRepository
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import co.netguru.baby.monitor.client.feature.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcClient
import co.netguru.baby.monitor.client.feature.communication.websocket.ConnectionStatus
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.single.SingleDefer
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
        private val childRepository: ChildRepository
) : ViewModel() {

    internal val selectedChild = MutableLiveData<ChildData>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()
    internal val selectedChildAvailability = MutableLiveData<ConnectionStatus>()
    internal val childList: MutableLiveData<List<ChildData>>
        get() = childRepository.childList

    private val compositeDisposable = CompositeDisposable()
    private var currentCall: RtcClient? = null

    //TODO change it for real data fetch
    val activities: LiveData<List<LogActivityData.LogData>> = Transformations.switchMap(selectedChild) { child ->
        child ?: return@switchMap MutableLiveData<List<LogActivityData.LogData>>()
        return@switchMap Transformations.map(LogActivityData.getSampleData()) { activitiesList ->
            return@map activitiesList.map { LogActivityData.LogData(it.action, it.timeStamp, child.image) }
        }
    }

    fun refreshChildrenList(listener: (state: List<ChildData>) -> Unit = {}) {
        childRepository.refreshChildData()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { list ->
                            if (selectedChild.value == null && list.isNotEmpty()) {
                                selectedChild.postValue(list.first())
                            }
                            listener(list)
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    fun setSelectedChildWithAddress(address: String) {
        childRepository.childList.value?.find { it.address == address }?.let { data ->
            selectedChild.postValue(data)
            refreshChildrenList()
        }
    }

    fun updateChildName(name: String) {
        Single.just(selectedChild.value).flatMap { data ->
            childRepository.updateChildData(data.apply { this.name = name })
        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { data ->
                            selectedChild.postValue(data)
                        }
                )
                .addTo(compositeDisposable)
    }

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

    fun callCleanUp(onCleaned: () -> Unit) {
        with(currentCall) {
            if (this == null) {
                onCleaned()
            } else {
                cleanup(disposeConnection = true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onComplete = onCleaned,
                                onError = Timber::e
                        ).addTo(compositeDisposable)
            }
        }
    }

    private fun updateChildImageSource(path: String) {
        Single.just(selectedChild.value).flatMap { data ->
            childRepository.updateChildData(data.apply { image = path })
        }.subscribeOn(Schedulers.io())
                .subscribeBy(onSuccess = { data ->
                    selectedChild.postValue(data)
                }).addTo(compositeDisposable)
    }

    fun setRemoteRenderer(remoteRenderer: SurfaceViewRenderer) {
        currentCall?.remoteRenderer = remoteRenderer
    }

    fun startCall(
            rtcClient: RtcClient,
            context: Context,
            listener: (state: CallState) -> Unit
    ) {
        currentCall = rtcClient
                .also { client ->
                    client.startCall(context, listener).subscribeOn(Schedulers.newThread())
                            .subscribeBy(
                                    onComplete = { Timber.i("completed") },
                                    onError = Timber::e
                            ).addTo(compositeDisposable)
                }
    }

    fun isBabyDataFilled(): Boolean {
        val child = selectedChild.value ?: return false
        return (child.image != null)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
