package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.common.Randomiser
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
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

class VoiceAnalysisUseCaseTest {

    private val dataRepository = mock<DataRepository> {
        on { updateVoiceAnalysisOption(any()) }.doReturn(Completable.complete())
    }
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
    private val messageController = mock<MessageController> {
        on { receivedMessages() }.doReturn(Observable.just(Message(confirmationId = confirmationId)))
    }
    private val randomiser = mock<Randomiser> {
        on { getRandomDigits(any()) }.doReturn(randomDigitsList)
    }

    private val voiceAnalysisUseCase =
        VoiceAnalysisUseCase(dataRepository, schedulersProvider, randomiser)

    @Test
    fun `should send noiseDetection message to baby device`() {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.NOISE_DETECTION
        ).test()
            .assertComplete()

        verify(messageController)
            .sendMessage(argThat { voiceAnalysisOption == VoiceAnalysisOption.NOISE_DETECTION.name })
    }

    @Test
    fun `should send machineLearning message to baby device`() {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MACHINE_LEARNING
        ).test()
            .assertComplete()

        verify(messageController).sendMessage(argThat { voiceAnalysisOption ==
                VoiceAnalysisOption.MACHINE_LEARNING.name })
    }

    @Test
    fun `should get successful response with the same confirmationId`() {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MACHINE_LEARNING
        ).test()
            .assertValue(true)
      
        verify(messageController).sendMessage(argThat { voiceAnalysisOption ==
                VoiceAnalysisOption.MACHINE_LEARNING.name })
    }

    @Test
    fun `should get failure response after timeout if correct confirmationId isn't returned`() {
        whenever(messageController.receivedMessages()).doReturn(
            Observable.just(
                Message(
                    confirmationId = "wrongId"
                )
            )
        )

        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MACHINE_LEARNING
        ).test()
            .assertValue(false)

        timerTestScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        
        verify(messageController).sendMessage(argThat { voiceAnalysisOption ==
                VoiceAnalysisOption.MACHINE_LEARNING.name })
    }

    @Test
    fun `should save successful option change to database`() {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MACHINE_LEARNING
        ).test()
            .assertValue(true)

        verify(dataRepository).updateVoiceAnalysisOption(VoiceAnalysisOption.MACHINE_LEARNING)
    }

    @Test
    fun `shouldn't save option change to database when change failed`() {
        whenever(messageController.receivedMessages()).doReturn(
            Observable.just(
                Message(
                    confirmationId = "wrongId"
                )
            )
        )
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MACHINE_LEARNING
        ).test()
            .assertValue(false)

        verifyZeroInteractions(dataRepository)
    }

    @Test
    fun `should sent initial option`() {
        val initialOption = VoiceAnalysisOption.MACHINE_LEARNING
        val childData = mock<ChildDataEntity> {
            on { voiceAnalysisOption }.doReturn(initialOption)
        }
        whenever(dataRepository.getChildData()).doReturn(Maybe.just(childData))
        voiceAnalysisUseCase
            .sendInitialVoiceAnalysisOption(rxWebSocketClient)
            .test()
            .assertComplete()

        verify(dataRepository).getChildData()
        verify(rxWebSocketClient).send(check {
            assert(it.voiceAnalysisOption == initialOption.name)
        })
    }
}
