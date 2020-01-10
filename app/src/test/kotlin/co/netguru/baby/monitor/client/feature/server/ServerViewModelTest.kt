package co.netguru.baby.monitor.client.feature.server

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.data.server.CameraState
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.server.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.Message.Companion.RESET_ACTION
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.server.ServerViewModel.Companion.VIDEO_PREVIEW_TOTAL_TIME
import com.nhaarman.mockitokotlin2.*
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.java_websocket.WebSocket
import org.junit.Rule
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class ServerViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val timerTestScheduler = TestScheduler()
    private val nsdServiceManager: NsdServiceManager = mock()
    private val dataRepository: DataRepository = mock()
    private val schedulersProvider: ISchedulersProvider = mock {
        on { io() } doReturn Schedulers.trampoline()
        on { mainThread() } doReturn Schedulers.trampoline()
        on { computation() } doReturn timerTestScheduler
    }
    private val deviceAddress = "deviceAddress"
    private val firebaseToken = "firebaseToken"
    private val receiveFirebaseTokenUseCase: ReceiveFirebaseTokenUseCase = mock {
        on { receiveToken(deviceAddress, firebaseToken) }.doReturn(Completable.complete())
    }
    private val lazyReceiveFirebaseTokenUseCase: Lazy<ReceiveFirebaseTokenUseCase> = mock {
        on { get() }.doReturn(receiveFirebaseTokenUseCase)
    }

    private val websocket: WebSocket = mock {
        val inetSocketAddress: InetSocketAddress = mock {
            val inetAddress: InetAddress = mock {
                on { hostAddress }.doReturn(deviceAddress)
            }
            on { address }.doReturn(inetAddress)
        }
        on { remoteSocketAddress }.doReturn(inetSocketAddress)
    }

    private val webSocketServiceBinder: WebSocketServerService.Binder = mock {
        on { clientConnectionStatus() }.doReturn(
            Observable.just(
                ClientConnectionStatus.CLIENT_CONNECTED
            )
        )
        on { messages() }.doReturn(Observable.just(websocket to mock()))
    }

    private val serverViewModel =
        ServerViewModel(
            nsdServiceManager,
            dataRepository,
            lazyReceiveFirebaseTokenUseCase,
            schedulersProvider
        )

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
        serverViewModel.registerNsdService()

        verify(nsdServiceManager).registerService()
    }

    @Test
    fun `should unregister NsdService using NsdServiceManager`() {
        serverViewModel.unregisterNsdService()

        verify(nsdServiceManager).unregisterService()
    }

    @Test
    fun `should save configuration to dataRepository`() {
        whenever(dataRepository.saveConfiguration(AppState.SERVER)).doReturn(Completable.complete())

        serverViewModel.saveConfiguration()

        verify(dataRepository).saveConfiguration(AppState.SERVER)
    }

    @Test
    fun `should handle RTC connected state`() {
        val cameraStateObserver: Observer<CameraState> = mock()
        val rtcConnectionStateObserver: Observer<RtcConnectionState> = mock()
        val webRtcServiceBinder: WebRtcService.Binder = mock()
        whenever(webRtcServiceBinder.getConnectionObservable()).doReturn(
            Observable.just<RtcConnectionState>(
                RtcConnectionState.Connected
            )
        )

        serverViewModel.cameraState.observeForever(cameraStateObserver)
        assert(serverViewModel.cameraState.value?.streamingEnabled == false)
        serverViewModel.handleRtcServerConnectionState(webRtcServiceBinder)
        serverViewModel.rtcConnectionStatus.observeForever(rtcConnectionStateObserver)

        verify(cameraStateObserver, times(2)).onChanged(any())
        verify(rtcConnectionStateObserver).onChanged(RtcConnectionState.Connected)
        assert(serverViewModel.cameraState.value?.streamingEnabled == true)
    }

    @Test
    fun `should handle RTC disconnected state`() {
        val cameraStateObserver: Observer<CameraState> = mock()
        val rtcConnectionStateObserver: Observer<RtcConnectionState> = mock()
        val webRtcServiceBinder: WebRtcService.Binder = mock()
        whenever(webRtcServiceBinder.getConnectionObservable()).doReturn(
            Observable.just<RtcConnectionState>(
                RtcConnectionState.Disconnected
            )
        )

        serverViewModel.cameraState.observeForever(cameraStateObserver)
        assert(serverViewModel.cameraState.value?.streamingEnabled == false)
        serverViewModel.handleRtcServerConnectionState(webRtcServiceBinder)
        serverViewModel.rtcConnectionStatus.observeForever(rtcConnectionStateObserver)

        verify(cameraStateObserver, times(2)).onChanged(any())
        verify(rtcConnectionStateObserver).onChanged(RtcConnectionState.Disconnected)
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

    @Test
    fun `should handle Firebase token`() {
        val message = Message(pushNotificationsToken = firebaseToken)
        whenever(webSocketServiceBinder.messages()).doReturn(Observable.just(websocket to message))

        serverViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(lazyReceiveFirebaseTokenUseCase).get()
        verify(receiveFirebaseTokenUseCase).receiveToken(deviceAddress, firebaseToken)
    }

    @Test
    fun `should update pulsating view state on connnection status`() {
        val clientConnectionStatusObserver: Observer<ClientConnectionStatus> = mock()
        serverViewModel.pulsatingViewStatus.observeForever(clientConnectionStatusObserver)
        whenever(webSocketServiceBinder.clientConnectionStatus()).doReturn(
            Observable.just(
                ClientConnectionStatus.CLIENT_CONNECTED
            )
        )

        serverViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(clientConnectionStatusObserver).onChanged(ClientConnectionStatus.CLIENT_CONNECTED)

        whenever(webSocketServiceBinder.clientConnectionStatus()).doReturn(
            Observable.just(
                ClientConnectionStatus.EMPTY
            )
        )

        serverViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(clientConnectionStatusObserver).onChanged(ClientConnectionStatus.EMPTY)
    }

    @Test
    fun `should update baby name status on binder messages`() {
        val name = "babyName"
        val babyNameObserver: Observer<String> = mock()
        val message: Message = mock {
            on { babyName }.doReturn(name)
        }
        whenever(webSocketServiceBinder.messages()).doReturn(Observable.just(websocket to message))
        serverViewModel.babyNameStatus.observeForever(babyNameObserver)

        serverViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(babyNameObserver).onChanged(name)
    }

    @Test
    fun `should notify observers about web socket reset action`() {
        val resetActionObserver: Observer<String> = mock()
        val message: Message = mock {
            on { action }.doReturn(RESET_ACTION)
        }
        whenever(webSocketServiceBinder.messages()).doReturn(Observable.just(websocket to message))
        serverViewModel.webSocketAction.observeForever(resetActionObserver)

        serverViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(resetActionObserver).onChanged(RESET_ACTION)
    }
}
