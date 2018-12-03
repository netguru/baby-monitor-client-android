package co.netguru.baby.monitor.client.feature.communication.websocket

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.nhaarman.mockito_kotlin.any
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class EventProcessorTest {

    @Mock
    lateinit var gsonMock: Gson

    @InjectMocks
    lateinit var eventProcessor: EventProcessor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `Baby is crying action should be properly parsed to event`() {
        `when`(
                gsonMock.fromJson(
                        any<String>(),
                        any<Class<MessageCommand>>()
                )
        ).then { return@then MessageCommand(MessageAction.BABY_IS_CRYING, null) }

        val messageAction = eventProcessor.process("TEST_MESSAGE")
        assertEquals(MessageAction.BABY_IS_CRYING, messageAction)
    }

    @Test
    fun `When message is nullable action should be null`() {
        val messageAction = eventProcessor.process(null)
        assertEquals(null, messageAction)
    }

    @Test
    fun `When exception on parsing action should be null`() {
        `when`(
                gsonMock.fromJson(
                        any<String>(),
                        any<Class<MessageCommand>>()
                )
        ).thenThrow(JsonSyntaxException("Test exception"))

        val messageAction = eventProcessor.process("TEST_MESSAGE")
        assertEquals(null, messageAction)
    }

}
