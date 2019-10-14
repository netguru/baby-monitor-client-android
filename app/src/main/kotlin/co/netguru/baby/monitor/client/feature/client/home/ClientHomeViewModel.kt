package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.home.ToolbarState
import co.netguru.baby.monitor.client.data.client.home.log.LogData
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {

    internal val logData = MutableLiveData<List<LogData>>()
    internal val selectedChild = dataRepository.getChildLiveData()
    internal val selectedChildAvailability = MutableLiveData<Boolean>()
    internal val toolbarState = MutableLiveData<ToolbarState>()
    internal val shouldDrawerBeOpen = MutableLiveData<Boolean>()
    internal val backButtonShouldBeVisible = MutableLiveData<Boolean>()

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
        compositeDisposable.dispose()
    }

    @RunsInBackground
    private fun handleNextLogDataList(list: List<LogDataEntity>) {
        logData.postValue(list.map { data ->
            data.toLogData(selectedChild.value?.image)
        })
    }

    fun showBackButton(shouldBeVisible: Boolean) {
        backButtonShouldBeVisible.postValue(shouldBeVisible)
    }
}
