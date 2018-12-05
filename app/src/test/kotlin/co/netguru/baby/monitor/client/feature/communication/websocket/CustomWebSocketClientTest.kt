package co.netguru.baby.monitor.client.feature.communication.websocket

import com.nhaarman.mockito_kotlin.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CustomWebSocketClientTest {

    @Mock
    lateinit var listener: WebSocketCommunicationListener

    lateinit var client: CustomWebSocketClient

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        client = CustomWebSocketClient("1.1.1.1", listener)
    }

    @Test
    fun `When connection is open, listener should be notified about status CONNECTED`() {
        client.onOpen(mock())

        verify(listener).onAvailabilityChanged(client, ConnectionStatus.CONNECTED)
    }

    @Test
    fun `When connection is closed, listener should be notified about status DISCONNECTED`() {
        client.onClose(0, "", false)

        verify(listener).onAvailabilityChanged(client, ConnectionStatus.DISCONNECTED)
    }

    @Test
    fun `Listener should not be notified if status has been changed to the same one`() {
        client.availability = ConnectionStatus.CONNECTED
        client.onOpen(mock())

        verifyZeroInteractions(listener)
    }

    @Test
    fun `Listener should be notified about new message`() {
        client.onMessage("Test")
        verify(listener).onMessageReceived(client, "Test")
    }

    @Test
    fun `Additional listener should be notified about message`() {
        val mockListener = mock<(CustomWebSocketClient, String?) -> Unit>()

        client.addMessageListener(mockListener)
        client.onMessage("Test")

        verify(listener).onMessageReceived(client, "Test")
        verify(mockListener).invoke(client, "Test")
    }

    @Test
    fun `When exception occures client should be disconnected`() {
        client.onError(Exception("Test"))
        verify(listener).onAvailabilityChanged(client, ConnectionStatus.DISCONNECTED)
    }
}
