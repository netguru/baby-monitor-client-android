package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogData
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClient
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.data.splash.AppState
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.single.SingleDefer
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
        private val dataRepository: DataRepository
) : ViewModel() {

    internal val logData = MutableLiveData<List<LogData>>()
    internal val selectedChild = MutableLiveData<ChildDataEntity>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()
    internal val selectedChildAvailability = MutableLiveData<ConnectionStatus>()
    internal val childList = MutableLiveData<List<ChildDataEntity>>()

    private val compositeDisposable = CompositeDisposable()
    private var currentCall: RtcClient? = null

    init {
        dataRepository.getChildData()
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onNext = { list ->
                            if (selectedChild.value == null && list.isNotEmpty()) {
                                selectedChild.postValue(list.first())
                            }
                            childList.postValue(list)
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    fun setSelectedChildWithAddress(address: String) {
        childList.value?.find { it.address == address }?.let { data ->
            selectedChild.postValue(data)
        }
    }

    fun updateChildName(name: String) {
        Single.just(selectedChild.value).flatMap { data ->
            dataRepository.updateChildData(data.apply { this.name = name })
        }.subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { data ->
                            selectedChild.postValue(data)
                        },
                        onError = Timber::e
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
            dataRepository.updateChildData(data.apply { image = path })
        }.subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { data ->
                            selectedChild.postValue(data)
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)
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

    fun selectedChildAvailabilityPostValue(data: Pair<ChildDataEntity, ConnectionStatus>) {
        if (data.first.address == selectedChild.value?.address &&
                data.second != selectedChildAvailability.value) {
            selectedChildAvailability.postValue(data.second)
        }
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
        compositeDisposable.dispose()
    }

    @RunsInBackground
    private fun handleNextLogDataList(list: List<LogDataEntity>) {
        logData.postValue(list.map { data ->
            data.toLogData(childList.value?.find { data.address == it.address }?.image)
        })
    }
}
