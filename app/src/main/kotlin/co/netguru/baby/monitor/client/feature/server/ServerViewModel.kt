package co.netguru.baby.monitor.client.feature.server

import androidx.lifecycle.*
import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.server.CameraState
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.server.WebRtcService
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ServerViewModel @Inject constructor(
    private val nsdServiceManager: NsdServiceManager,
    private val dataRepository: DataRepository,
    private val schedulersProvider: ISchedulersProvider
) : ViewModel() {

    private val mutableShouldDrawerBeOpen = MutableLiveData<Boolean>()
    internal val shouldDrawerBeOpen: LiveData<Boolean> = mutableShouldDrawerBeOpen
    private val mutableTimer = MutableLiveData<Long>()
    private var timerDisposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()
    private val mutableRtcConnectionStatus = MutableLiveData<RtcConnectionState>()
    private val mutableCameraState = MutableLiveData(
        CameraState(
            previewEnabled = true,
            streamingEnabled = false
        )
    )
    internal val cameraState: LiveData<CameraState> = mutableCameraState
    val rtcConnectionStatus: LiveData<RtcConnectionState> = mutableRtcConnectionStatus

    internal val previewingVideo = Transformations.map(cameraState) { it.previewEnabled }
        .distinctUntilChanged()

    internal val timer: LiveData<Long> =
        mutableTimer

    internal fun resetTimer() {
        timerDisposable?.dispose()
        timerDisposable = Observable.intervalRange(1, VIDEO_PREVIEW_TOTAL_TIME, 0, 1, TimeUnit.SECONDS,
            schedulersProvider.computation())
            .observeOn(schedulersProvider.mainThread())
            .subscribeBy(
                onNext = { elapsedSeconds ->
                    val secondsLeft = VIDEO_PREVIEW_TOTAL_TIME - elapsedSeconds
                    mutableTimer.postValue(secondsLeft)
                },
                onComplete = {
                    mutableTimer.postValue(null)
                    toggleVideoPreview(false)
                }
            )
    }

    internal fun registerNsdService(
        onRegistrationFailed: (errorCode: Int) -> Unit
    ) {
        nsdServiceManager.registerService(object : NsdServiceManager.OnServiceConnectedListener {
            override fun onServiceConnectionError(errorCode: Int) {
                Timber.e("Service connection error with error code: $errorCode")
            }
            override fun onStartDiscoveryFailed(errorCode: Int) {
                Timber.e("Service registration failed with error code: $errorCode")
            }

            override fun onRegistrationFailed(errorCode: Int) {
                onRegistrationFailed(errorCode)
            }
        })
    }

    internal fun unregisterNsdService() {
        nsdServiceManager.unregisterService()
    }

    internal fun saveConfiguration() {
        dataRepository.saveConfiguration(AppState.SERVER)
            .subscribeOn(schedulersProvider.io())
            .subscribeBy(
                onComplete = { Timber.i("state saved") },
                onError = Timber::e
            ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun handleRtcServerConnectionState(webRtcServiceBinder: WebRtcService.Binder) {
        compositeDisposable += webRtcServiceBinder.getConnectionObservable()
            .subscribeOn(schedulersProvider.io())
            .subscribeBy(
                onNext = {
                    handleStreamState(it)
                    mutableRtcConnectionStatus.postValue(it)
                },
                onError = { mutableRtcConnectionStatus.postValue(RtcConnectionState.Error) }
            )
    }

    fun toggleDrawer(shouldBeOpened: Boolean) {
        mutableShouldDrawerBeOpen.postValue(shouldBeOpened)
    }

    fun toggleVideoPreview(shouldShowPreview: Boolean) {
        handleCameraState(previewEnabled = shouldShowPreview)
        if (!shouldShowPreview && timerDisposable?.isDisposed == false) timerDisposable?.dispose()
    }

    private fun handleCameraState(
        previewEnabled: Boolean = mutableCameraState.value?.previewEnabled == true,
        streamingEnabled: Boolean = mutableCameraState.value?.streamingEnabled == true
    ) {
        mutableCameraState.postValue(CameraState(previewEnabled, streamingEnabled))
    }

    private fun handleStreamState(it: RtcConnectionState?) {
        when (it) {
            RtcConnectionState.Connected -> handleCameraState(streamingEnabled = true)
            RtcConnectionState.Disconnected -> handleCameraState(streamingEnabled = false)
        }
    }

    companion object {
        const val VIDEO_PREVIEW_TOTAL_TIME = 65L
    }
}
