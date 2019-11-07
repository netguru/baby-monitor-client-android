package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.common.proto.Message
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SendBabyNameUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val childDataEntity = ChildDataEntity("", name = "name")
    private val dataRepository: DataRepository = mock {
        on { getChildData() }.doReturn(Maybe.just(childDataEntity))
    }
    private val message = "message"
    private val gson: Gson = mock {
        on { toJson(any<Message>()) }.doReturn(message)
    }
    private val rxWebSocketClient: RxWebSocketClient = mock {
        on { send(any()) }.doReturn(Completable.complete())
    }
    private val sendBabyNameUseCase = SendBabyNameUseCase(dataRepository, gson)

    @Test
    fun `should send baby name message`() {
        sendBabyNameUseCase
            .streamBabyName(rxWebSocketClient)
            .test()
            .assertComplete()

        verify(gson).toJson(check<Message> {
            assertEquals(childDataEntity.name, it.babyName)
        })
        verify(dataRepository).getChildData()
        verify(rxWebSocketClient).send(message)
    }
}
