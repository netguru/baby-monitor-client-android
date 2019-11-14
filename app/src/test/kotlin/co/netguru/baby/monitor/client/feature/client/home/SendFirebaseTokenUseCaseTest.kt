package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

class SendFirebaseTokenUseCaseTest {

    private val token = "token"
    private val firebaseInstanceManager: FirebaseInstanceManager = mock {
        on { getFirebaseToken() }.doReturn(Single.just(token))
    }
    private val rxWebSocketClient: RxWebSocketClient = mock {
        on { send(any()) }.doReturn(Completable.complete())
    }
    private val sendFirebaseTokenUseCase = SendFirebaseTokenUseCase(firebaseInstanceManager)

    @Test
    fun sendFirebaseToken() {
        sendFirebaseTokenUseCase.sendFirebaseToken(rxWebSocketClient)
            .test()
            .assertComplete()

        verify(rxWebSocketClient).send(argThat { contains(token) })
    }
}
