package co.netguru.baby.monitor.client.feature.server

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.ENABLED_PARAM
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.NIGHT_MODE_EVENT
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class ChildMonitorViewModel @Inject constructor(
    private val notifyLowBatteryUseCase: NotifyLowBatteryUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val mutableNightModeStatus = MutableLiveData<Boolean>()
    internal val nightModeStatus: LiveData<Boolean> = mutableNightModeStatus

    fun notifyLowBattery(title: String, text: String) {
        disposables += notifyLowBatteryUseCase.notifyLowBattery(title = title, text = text)
            .subscribeBy(
                onComplete = {
                    Timber.d("Low battery notification posted.")
                },
                onError = { error ->
                    Timber.w(error, "Error posting low battery notification.")
                }
            )
    }

    fun switchNightMode() {
        val currentStatus = mutableNightModeStatus.value == true
        analyticsManager.logEvent(NIGHT_MODE_EVENT, bundleOf(ENABLED_PARAM to !currentStatus))
        mutableNightModeStatus.postValue(!currentStatus)
    }

    override fun onCleared() {
        disposables.clear()
    }
}
