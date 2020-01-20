package co.netguru.baby.monitor.client.feature.client.home

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.feature.babycrynotification.SnoozeNotificationUseCase
import co.netguru.baby.monitor.client.feature.communication.internet.CheckInternetConnectionUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.Message.Companion.RESET_ACTION
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageParser
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import java.net.URI

class ClientHomeViewModelTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val childLiveData: LiveData<ChildDataEntity> = MutableLiveData()
    private val dataRepository: DataRepository = mock {
        on { getChildLiveData() }.doReturn(childLiveData)
    }
    private val rxWebSocketClient: RxWebSocketClient = mock()

    private val sendBabyNameUseCase: SendBabyNameUseCase = mock {
        on { streamBabyName(rxWebSocketClient) }.doReturn(Completable.complete())
    }
    private val snoozeNotificationUseCase: SnoozeNotificationUseCase = mock()
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase = mock()
    private val restartAppUseCase: RestartAppUseCase = mock()
    private val messageParser: MessageParser = mock()

    private val clientHomeViewModel = ClientHomeViewModel(
        dataRepository,
        sendBabyNameUseCase,
        snoozeNotificationUseCase,
        checkInternetConnectionUseCase,
        restartAppUseCase,
        rxWebSocketClient,
        messageParser
    )

    @Test
    fun `should handle web socket open connection`() {
        val uri: URI = mock()
        val selectedChildAvailabilityObserver: Observer<Boolean> = mock()
        whenever(rxWebSocketClient.events(uri)).doReturn(
            Observable.just<RxWebSocketClient.Event>(
                RxWebSocketClient.Event.Open
            )
        )
        clientHomeViewModel.selectedChildAvailability.observeForever(
            selectedChildAvailabilityObserver
        )

        clientHomeViewModel.openSocketConnection(uri)

        verify(selectedChildAvailabilityObserver).onChanged(true)
        verify(sendBabyNameUseCase).streamBabyName(rxWebSocketClient)
    }

    @Test
    fun `should handle web socket closed connection`() {
        val uri: URI = mock()
        val selectedChildAvailabilityObserver: Observer<Boolean> = mock()
        val closeEvent: RxWebSocketClient.Event.Close = mock()
        whenever(rxWebSocketClient.events(uri)).doReturn(
            Observable.just<RxWebSocketClient.Event>(
                closeEvent
            )
        )
        clientHomeViewModel.selectedChildAvailability.observeForever(
            selectedChildAvailabilityObserver
        )

        clientHomeViewModel.openSocketConnection(uri)

        verify(selectedChildAvailabilityObserver).onChanged(false)
    }

    @Test
    fun `should notify observers about web socket reset action`() {
        val uri: URI = mock()
        val resetActionObserver: Observer<String> = mock()
        val resetAction: RxWebSocketClient.Event.Message = mock()
        whenever(rxWebSocketClient.events(uri)).doReturn(
            Observable.just<RxWebSocketClient.Event>(
                resetAction
            )
        )
        whenever(messageParser.parseWebSocketMessage(resetAction)).doReturn(
            Message(action = RESET_ACTION)
        )
        clientHomeViewModel.webSocketAction.observeForever(
            resetActionObserver
        )

        clientHomeViewModel.openSocketConnection(uri)

        verify(resetActionObserver).onChanged(check {
            assertEquals(RESET_ACTION, it)
        })
    }

    @Test
    fun `should snooze notifications`() {
        val disposable = mock<Disposable>()
        whenever(snoozeNotificationUseCase.snoozeNotifications()).doReturn(disposable)
        clientHomeViewModel.snoozeNotifications()

        verify(snoozeNotificationUseCase).snoozeNotifications()
    }

    @Test
    fun `should check internet connection status`() {
        val internetConnectionObserver: Observer<Boolean> = mock()
        whenever(checkInternetConnectionUseCase.hasInternetConnection()).doReturn(Single.just(true))
        clientHomeViewModel.internetConnectionAvailability.observeForever(internetConnectionObserver)

        clientHomeViewModel.checkInternetConnection()

        verify(checkInternetConnectionUseCase).hasInternetConnection()
        verify(internetConnectionObserver).onChanged(true)
    }

    @Test
    fun `should restart app`() {
        val activity: AppCompatActivity = mock()
        whenever(restartAppUseCase.restartApp(activity)).doReturn(Completable.complete())

        clientHomeViewModel.restartApp(activity)

        verify(restartAppUseCase).restartApp(activity)
    }
}
