package co.netguru.baby.monitor.client.feature.server

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.server.CameraState
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.RtcServerConnectionState
import co.netguru.baby.monitor.client.feature.server.ServerViewModel.Companion.VIDEO_PREVIEW_TOTAL_TIME
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class ServerViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var serverViewModel: ServerViewModel
    private val nsdServiceManager: NsdServiceManager = mock()
    private val dataRepository: DataRepository = mock()
    private val schedulersProvider: ISchedulersProvider = mock {
        on { io() } doReturn Schedulers.trampoline()
        on { mainThread() } doReturn Schedulers.trampoline()
        on { computation() } doReturn Schedulers.trampoline()
    }
    private val timerTestScheduler = TestScheduler()

    @Before
    fun setup() {
        serverViewModel = ServerViewModel(nsdServiceManager, dataRepository, schedulersProvider)
        whenever(schedulersProvider.mainThread()).doReturn(Schedulers.trampoline())
        whenever(schedulersProvider.io()).doReturn(Schedulers.trampoline())
        whenever(schedulersProvider.computation()).doReturn(timerTestScheduler)
    }

    @Test
    fun `should disable preview after VIDEO_PREVIEW_TOTAL_TIME`() {
        val cameraStateObserver: Observer<CameraState> = mock()
        serverViewModel.cameraState.observeForever(cameraStateObserver)

        assert(serverViewModel.cameraState.value?.previewEnabled == true)
        serverViewModel.resetTimer()
        timerTestScheduler.advanceTimeBy(VIDEO_PREVIEW_TOTAL_TIME, TimeUnit.SECONDS)

        verify(cameraStateObserver, times(2)).onChanged(any())
        assert(serverViewModel.cameraState.value?.previewEnabled == false)
    }

    @Test
    fun `should update timer value`() {
        val passedTime = 5L
        val startingFromOne = 1

        serverViewModel.resetTimer()
        timerTestScheduler.advanceTimeBy(passedTime, TimeUnit.SECONDS)

        assert(serverViewModel.timer.value == VIDEO_PREVIEW_TOTAL_TIME - passedTime - startingFromOne)
    }

    @Test
    fun `should register NsdService using NsdServiceManager`() {
        serverViewModel.registerNsdService { }

        verify(nsdServiceManager).registerService(any())
    }

    @Test
    fun `should unregister NsdService using NsdServiceManager`() {
        serverViewModel.unregisterNsdService()

        verify(nsdServiceManager).unregisterService()
    }

    @Test
    fun `should save configuration to dataRepository`() {
        whenever(dataRepository.saveConfiguration(AppState.SERVER)).thenReturn(Completable.complete())

        serverViewModel.saveConfiguration()

        verify(dataRepository).saveConfiguration(AppState.SERVER)
    }

    @Test
    fun `should handle RTC connected state`() {
        val cameraStateObserver: Observer<CameraState> = mock()
        val rtcConnectionStateObserver: Observer<RtcServerConnectionState> = mock()
        val webRtcServiceBinder: WebRtcService.Binder = mock()
        whenever(webRtcServiceBinder.getConnectionObservable()).thenReturn(
            Observable.just(
                RtcServerConnectionState.Connected
            )
        )

        serverViewModel.cameraState.observeForever(cameraStateObserver)
        assert(serverViewModel.cameraState.value?.streamingEnabled == false)
        serverViewModel.handleRtcServerConnectionState(webRtcServiceBinder)
        serverViewModel.rtcConnectionStatus.observeForever(rtcConnectionStateObserver)

        verify(cameraStateObserver, times(2)).onChanged(any())
        verify(rtcConnectionStateObserver).onChanged(RtcServerConnectionState.Connected)
        assert(serverViewModel.cameraState.value?.streamingEnabled == true)
    }

    @Test
    fun `should handle RTC disconnected state`() {
        val cameraStateObserver: Observer<CameraState> = mock()
        val rtcConnectionStateObserver: Observer<RtcServerConnectionState> = mock()
        val webRtcServiceBinder: WebRtcService.Binder = mock()
        whenever(webRtcServiceBinder.getConnectionObservable()).thenReturn(
            Observable.just(
                RtcServerConnectionState.Disconnected
            )
        )

        serverViewModel.cameraState.observeForever(cameraStateObserver)
        assert(serverViewModel.cameraState.value?.streamingEnabled == false)
        serverViewModel.handleRtcServerConnectionState(webRtcServiceBinder)
        serverViewModel.rtcConnectionStatus.observeForever(rtcConnectionStateObserver)

        verify(cameraStateObserver, times(2)).onChanged(any())
        verify(rtcConnectionStateObserver).onChanged(RtcServerConnectionState.Disconnected)
        assert(serverViewModel.cameraState.value?.streamingEnabled == false)
    }

    @Test
    fun `should change camera state on toggleVideoPreview`() {
        val cameraStateObserver: Observer<CameraState> = mock()

        serverViewModel.cameraState.observeForever(cameraStateObserver)
        serverViewModel.toggleVideoPreview(true)

        assert(serverViewModel.cameraState.value?.previewEnabled == true)

        serverViewModel.toggleVideoPreview(false)

        assert(serverViewModel.cameraState.value?.previewEnabled == false)
        verify(cameraStateObserver, times(3)).onChanged(any())
    }

    @Test
    fun `should change drawer state on toggleDrawer`() {
        val drawerStateObserver: Observer<Boolean> = mock()

        serverViewModel.shouldDrawerBeOpen.observeForever(drawerStateObserver)

        assert(serverViewModel.shouldDrawerBeOpen.value != true)

        serverViewModel.toggleDrawer(true)

        assert(serverViewModel.shouldDrawerBeOpen.value == true)
        verify(drawerStateObserver).onChanged(true)

        serverViewModel.toggleDrawer(false)

        assert(serverViewModel.shouldDrawerBeOpen.value == false)
        verify(drawerStateObserver).onChanged(false)
    }
}
