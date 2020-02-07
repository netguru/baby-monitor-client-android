package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.feature.communication.ConfirmationItem
import co.netguru.baby.monitor.client.feature.communication.ConfirmationUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit

class ConfirmationUseCaseTest {

    private val dataRepository = mock<DataRepository>()
    private val randomDigitsList = listOf(1, 2, 3, 4)
    private val confirmationId = randomDigitsList.joinToString("")
    private val timerTestScheduler = TestScheduler()
    private val rxWebSocketClient: RxWebSocketClient = mock {
        on { send(any()) }.doReturn(Completable.complete())
    }
    private val schedulersProvider = mock<ISchedulersProvider> {
        on { io() } doReturn timerTestScheduler
        on { mainThread() } doReturn Schedulers.trampoline()
        on { computation() } doReturn Schedulers.trampoline()
    }
    private val message = Message(confirmationId = confirmationId)
    private val messageController = mock<MessageController> {
        on { receivedMessages() }.doReturn(Observable.just(message))
    }
    private val confirmationItem = mock<ConfirmationItem<VoiceAnalysisOption>> {
        on { onSuccessAction(dataRepository)}
            .doReturn(Completable.complete())
    }

    private val confirmationUseCase =
        ConfirmationUseCase(dataRepository, schedulersProvider)

    @Test
    fun `should send noiseDetection message to baby device`() {
        whenever(confirmationItem.sentMessage).doReturn(
            Message(confirmationId = confirmationId,
            voiceAnalysisOption = VoiceAnalysisOption.NOISE_DETECTION.name))
        confirmationUseCase.changeValue(
            messageController,
            confirmationItem
        ).test()
            .assertComplete()

        verify(messageController)
            .sendMessage(argThat { voiceAnalysisOption == VoiceAnalysisOption.NOISE_DETECTION.name })
    }

    @Test
    fun `should send machineLearning message to baby device`() {
        whenever(confirmationItem.sentMessage).doReturn(
            Message(confirmationId = confirmationId,
                voiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING.name))
        confirmationUseCase.changeValue(
            messageController,
            confirmationItem
        ).test()
            .assertComplete()

        verify(messageController).sendMessage(argThat { voiceAnalysisOption ==
                VoiceAnalysisOption.MACHINE_LEARNING.name })
    }

    @Test
    fun `should get successful response with the same confirmationId`() {
        whenever(confirmationItem.sentMessage).doReturn(
            Message(confirmationId = confirmationId,
                voiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING.name))
        confirmationUseCase.changeValue(
            messageController,
            confirmationItem
        ).test()
            .assertValue(true)
      
        verify(messageController).sendMessage(argThat { voiceAnalysisOption ==
                VoiceAnalysisOption.MACHINE_LEARNING.name })
    }

    @Test
    fun `should get failure response after timeout if correct confirmationId isn't returned`() {
        whenever(confirmationItem.sentMessage).doReturn(
            Message(confirmationId = "wrongId",
                voiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING.name))

        confirmationUseCase.changeValue(
            messageController,
            confirmationItem
        ).test()
            .assertValue(false)

        timerTestScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        
        verify(messageController).sendMessage(argThat { voiceAnalysisOption ==
                VoiceAnalysisOption.MACHINE_LEARNING.name })
    }

    @Test
    fun `should invoke succes action with data repository`() {
        whenever(confirmationItem.sentMessage).doReturn(
            Message(confirmationId = confirmationId,
                voiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING.name))

        confirmationUseCase.changeValue(
            messageController,
            confirmationItem
        ).test()
            .assertValue(true)

        verify(confirmationItem).onSuccessAction(dataRepository)
    }

    @Test
    fun `shouldn't save option change to database when change failed`() {
        whenever(confirmationItem.sentMessage).doReturn(
            Message(confirmationId = "wrongId",
                voiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING.name))

        confirmationUseCase.changeValue(
            messageController,
            confirmationItem
        ).test()
            .assertValue(false)

        verify(confirmationItem, times(0)).onSuccessAction(dataRepository)
    }
}
