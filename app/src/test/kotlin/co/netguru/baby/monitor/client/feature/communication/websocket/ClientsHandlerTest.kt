package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.feature.common.NotificationHandler
import com.nhaarman.mockito_kotlin.*
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class ClientsHandlerTest {

    @Mock
    lateinit var listener: (CustomWebSocketClient) -> Unit

    @Mock
    lateinit var notificationHandler: NotificationHandler

    @Mock
    lateinit var webSocketClientFactory: WebSocketClientFactory

    lateinit var clientsHandler: ClientsHandler

    private val testScheduler = TestScheduler()

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { scheduler -> testScheduler };
        RxJavaPlugins.setComputationSchedulerHandler { scheduler -> testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { scheduler -> testScheduler }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler -> testScheduler }

        MockitoAnnotations.initMocks(this)

        `when`(webSocketClientFactory.create(any(), any())).thenReturn(mock())

        clientsHandler = ClientsHandler(
                notificationHandler,
                webSocketClientFactory
        )

        clientsHandler.addConnectionListener(listener)
    }

    @Test
    fun `Clients list should be empty after clients handler init`() {
        assertEquals(0, clientsHandler.webSocketClients.size)
    }

    @Test
    fun `Adding client should be added as pair of address and connection`() {
        clientsHandler.addClient(DEFAULT_MOCK_ADDRESS).subscribe()
        assertNotNull(clientsHandler.webSocketClients[DEFAULT_MOCK_ADDRESS])
    }

    @Test
    fun `When received status is CONNECTED listener should be notified`() {
        val mockClient = mock<CustomWebSocketClient>()

        clientsHandler.onAvailabilityChanged(mockClient, ConnectionStatus.CONNECTED)
        verify(listener).invoke(mockClient)
    }

    @Test
    fun `When received status is DISCONNECTED, reconnect should happen`() {
        val oldClient = mock<CustomWebSocketClient>()
        `when`(oldClient.address).thenReturn(DEFAULT_MOCK_ADDRESS)

        clientsHandler.webSocketClients[DEFAULT_MOCK_ADDRESS] = oldClient
        clientsHandler.onAvailabilityChanged(oldClient, ConnectionStatus.DISCONNECTED)

        testScheduler.advanceTimeBy(50, TimeUnit.SECONDS)

        val newClient = clientsHandler.webSocketClients[DEFAULT_MOCK_ADDRESS]!!
        assertNotEquals(newClient, oldClient)

        verify(newClient).connect()
    }

    private fun putMockClientToClientsHandler(address: String = DEFAULT_MOCK_ADDRESS) {
        clientsHandler.webSocketClients[address] = mock()
    }

    companion object {
        const val DEFAULT_MOCK_ADDRESS = "1.1.1.1"
    }

}
