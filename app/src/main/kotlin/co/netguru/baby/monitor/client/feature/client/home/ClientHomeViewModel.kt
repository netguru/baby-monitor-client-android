package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.application.database.DataRepository
import co.netguru.baby.monitor.client.data.ChildData
import co.netguru.baby.monitor.client.data.ChildRepository
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogData
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataEntity
import co.netguru.baby.monitor.client.feature.common.RunsInBackground
import co.netguru.baby.monitor.client.feature.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClient
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
        private val childRepository: ChildRepository,
        private val dataRepository: DataRepository
) : ViewModel() {

    internal val logData = MutableLiveData<List<LogData>>()
    internal val selectedChild = MutableLiveData<ChildData>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()
    internal val selectedChildAvailability = MutableLiveData<ConnectionStatus>()
    internal val childList: MutableLiveData<List<ChildData>>
        get() = childRepository.childList

    private val compositeDisposable = CompositeDisposable()
    private var currentCall: RtcClient? = null

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

    fun isBabyDataFilled(): Boolean {
        val child = selectedChild.value ?: return false
        return (child.image != null)
    }

    fun fetchLogData() {
        dataRepository.getAllLogData()
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onNext = this::handleNextLogDataList,
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    fun selectedChildAvailabilityPostValue(data: Pair<ChildData, ConnectionStatus>) {
        if (data.first.address == selectedChild.value?.address &&
                data.second != selectedChildAvailability.value) {
            selectedChildAvailability.postValue(data.second)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    @RunsInBackground
    private fun handleNextLogDataList(list: List<LogDataEntity>) {
        logData.postValue(list.map { data ->
            data.toLogData(childList.value?.find { data.address == it.address }?.image)
        })
    }
}
