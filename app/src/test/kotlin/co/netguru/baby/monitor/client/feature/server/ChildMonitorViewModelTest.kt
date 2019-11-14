package co.netguru.baby.monitor.client.feature.server

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager.Companion.PUSH_NOTIFICATIONS_KEY
import com.nhaarman.mockitokotlin2.*
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import org.java_websocket.WebSocket
import org.junit.Rule
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress

class ChildMonitorViewModelTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val deviceAddress = "deviceAddress"
    private val firebaseToken = "firebaseToken"
    private val websocket: WebSocket = mock {
        val inetSocketAddress: InetSocketAddress = mock {
            val inetAddress: InetAddress = mock {
                on { hostAddress }.doReturn(deviceAddress)
            }
            on { address }.doReturn(inetAddress)
        }
        on { remoteSocketAddress }.doReturn(inetSocketAddress)
    }

    private val receiveFirebaseTokenUseCase: ReceiveFirebaseTokenUseCase = mock {
        on { receiveToken(deviceAddress, firebaseToken) }.doReturn(Completable.complete())
    }
    private val lazyReceiveFirebaseTokenUseCase: Lazy<ReceiveFirebaseTokenUseCase> = mock {
        on { get() }.doReturn(receiveFirebaseTokenUseCase)
    }
    private val notifyLowBatteryUseCase: NotifyLowBatteryUseCase = mock()
    private val webSocketServiceBinder: WebSocketServerService.Binder = mock {
        on { clientConnectionStatus() }.doReturn(
            Observable.just(
                ClientConnectionStatus.CLIENT_CONNECTED
            )
        )
        on { messages() }.doReturn(Observable.just(websocket to mock()))
    }

    private val childViewModel =
        ChildMonitorViewModel(lazyReceiveFirebaseTokenUseCase, notifyLowBatteryUseCase)

    @Test
    fun `should send Firebase token on PUSH_NOTIFICATIONS_KEY Websocket action`() {
        val message: Message = mock {
            on { action() }.doReturn(PUSH_NOTIFICATIONS_KEY to firebaseToken)
        }
        whenever(webSocketServiceBinder.messages()).doReturn(Observable.just(websocket to message))

        childViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(lazyReceiveFirebaseTokenUseCase).get()
        verify(receiveFirebaseTokenUseCase).receiveToken(deviceAddress, firebaseToken)
    }

    @Test
    fun `should not send Firebase token on key that isn't PUSH_NOTIFICATIONS_KEY`() {
        val notPushNotificationKey = "notPushNotificationKey"
        val message: Message = mock {
            on { action() }.doReturn(notPushNotificationKey to firebaseToken)
        }
        whenever(webSocketServiceBinder.messages()).doReturn(Observable.just(websocket to message))

        childViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verifyZeroInteractions(lazyReceiveFirebaseTokenUseCase)
        verifyZeroInteractions(receiveFirebaseTokenUseCase)
    }

    @Test
    fun `should update pulsating view state on connnection status`() {
        val clientConnectionStatusObserver: Observer<ClientConnectionStatus> = mock()
        childViewModel.pulsatingViewStatus.observeForever(clientConnectionStatusObserver)
        whenever(webSocketServiceBinder.clientConnectionStatus()).doReturn(
            Observable.just(
                ClientConnectionStatus.CLIENT_CONNECTED
            )
        )

        childViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(clientConnectionStatusObserver).onChanged(ClientConnectionStatus.CLIENT_CONNECTED)

        whenever(webSocketServiceBinder.clientConnectionStatus()).doReturn(
            Observable.just(
                ClientConnectionStatus.EMPTY
            )
        )

        childViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(clientConnectionStatusObserver).onChanged(ClientConnectionStatus.EMPTY)
    }

    @Test
    fun `should use notifyLowBatteryUseCase on notifyLowBattery`() {
        val title = "title"
        val text = "text"
        whenever(
            notifyLowBatteryUseCase.notifyLowBattery(
                title,
                text
            )
        ).doReturn(Completable.complete())

        childViewModel.notifyLowBattery(title, text)

        verify(notifyLowBatteryUseCase).notifyLowBattery(title, text)
    }

    @Test
    fun `should update baby name status on binder messages`() {
        val name = "babyName"
        val babyNameObserver: Observer<String> = mock()
        val message: Message = mock {
            on { action() }.doReturn("" to "")
            on { babyName }.doReturn(name)
        }
        whenever(webSocketServiceBinder.messages()).doReturn(Observable.just(websocket to message))
        childViewModel.babyNameStatus.observeForever(babyNameObserver)

        childViewModel.handleWebSocketServerBinder(webSocketServiceBinder)

        verify(babyNameObserver).onChanged(name)
    }

    @Test
    fun `should switch nightMode status`() {
        val nightModeObserver: Observer<Boolean> = mock()
        assert(childViewModel.nightModeStatus.value != true)
        childViewModel.nightModeStatus.observeForever(nightModeObserver)

        childViewModel.switchNightMode()

        verify(nightModeObserver).onChanged(true)
        assert(childViewModel.nightModeStatus.value == true)
    }
}
