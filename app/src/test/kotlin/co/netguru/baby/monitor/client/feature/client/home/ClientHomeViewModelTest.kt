package co.netguru.baby.monitor.client.feature.client.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.feature.babycrynotification.SnoozeNotificationUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URI

class ClientHomeViewModelTest {

    @Rule
    @JvmField
    val schedulersRule = RxSchedulersOverrideRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dataRepository: DataRepository = mock()
    private val rxWebSocketClient: RxWebSocketClient = mock()
    private val sendFirebaseTokenUseCase: SendFirebaseTokenUseCase = mock()
    private val sendBabyNameUseCase: SendBabyNameUseCase = mock()
    private val snoozeNotificationUseCase: SnoozeNotificationUseCase = mock()
    private val childLiveData: LiveData<ChildDataEntity> = MutableLiveData()
    private lateinit var clientHomeViewModel: ClientHomeViewModel

    @Before
    fun setUp() {
        whenever(dataRepository.getChildLiveData()).thenReturn(childLiveData)
        clientHomeViewModel = ClientHomeViewModel(
            dataRepository,
            sendFirebaseTokenUseCase,
            sendBabyNameUseCase,
            snoozeNotificationUseCase,
            rxWebSocketClient
        )
        whenever(sendFirebaseTokenUseCase.sendFirebaseToken(rxWebSocketClient)).thenReturn(
            Completable.complete())
        whenever(sendBabyNameUseCase.streamBabyName(rxWebSocketClient)).thenReturn(
            Completable.complete())
    }

    @Test
    fun `should handle web socket open connection`() {
        val uri: URI = mock()
        val selectedChildAvailabilityObserver: Observer<Boolean> = mock()
        whenever(rxWebSocketClient.events(uri)).thenReturn(Observable.just(RxWebSocketClient.Event.Open))
        clientHomeViewModel.selectedChildAvailability.observeForever(
            selectedChildAvailabilityObserver
        )

        clientHomeViewModel.openSocketConnection(uri)

        verify(selectedChildAvailabilityObserver).onChanged(true)
        verify(sendBabyNameUseCase).streamBabyName(rxWebSocketClient)
        verify(sendFirebaseTokenUseCase).sendFirebaseToken(rxWebSocketClient)
    }

    @Test
    fun `should handle web socket closed connection`() {
        val uri: URI = mock()
        val selectedChildAvailabilityObserver: Observer<Boolean> = mock()
        val closeEvent: RxWebSocketClient.Event.Close = mock()
        whenever(rxWebSocketClient.events(uri)).thenReturn(Observable.just(closeEvent))
        clientHomeViewModel.selectedChildAvailability.observeForever(
            selectedChildAvailabilityObserver
        )

        clientHomeViewModel.openSocketConnection(uri)

        verify(selectedChildAvailabilityObserver).onChanged(false)
        verifyZeroInteractions(sendFirebaseTokenUseCase)
    }

    @Test
    fun `should snooze notifications`() {
        val disposable = mock<Disposable>()
        whenever(snoozeNotificationUseCase.snoozeNotifications()).thenReturn(disposable)
        clientHomeViewModel.snoozeNotifications()

        verify(snoozeNotificationUseCase).snoozeNotifications()
    }
}
