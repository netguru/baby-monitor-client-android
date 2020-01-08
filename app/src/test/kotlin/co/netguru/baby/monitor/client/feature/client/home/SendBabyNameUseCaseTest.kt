package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.nhaarman.mockitokotlin2.*
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
    private val rxWebSocketClient: RxWebSocketClient = mock {
        on { send(any()) }.doReturn(Completable.complete())
    }
    private val sendBabyNameUseCase = SendBabyNameUseCase(dataRepository)

    @Test
    fun `should send baby name message`() {
        sendBabyNameUseCase
            .streamBabyName(rxWebSocketClient)
            .test()
            .assertComplete()

        verify(dataRepository).getChildData()
        verify(rxWebSocketClient).send(check {
            assertEquals(childDataEntity.name, it.babyName)
        })
    }
}
