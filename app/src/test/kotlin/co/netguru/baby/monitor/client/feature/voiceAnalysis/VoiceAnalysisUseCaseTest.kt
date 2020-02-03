package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.common.Randomiser
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
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
            VoiceAnalysisOption.NoiseDetection
        ).subscribe()

        verify(messageController).sendMessage(argThat { voiceAnalysisOption == VoiceAnalysisOption.NoiseDetection.name })
    }

    @Test
    fun `should send machineLearning message to baby device`() {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MachineLearning
        ).subscribe()

        verify(messageController).sendMessage(argThat { voiceAnalysisOption == VoiceAnalysisOption.MachineLearning.name })
    }

    @Test
    fun `should get successful response with the same confirmationId`() {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MachineLearning
        ).test()
            .assertValue(true)

        verify(messageController).sendMessage(argThat { voiceAnalysisOption == VoiceAnalysisOption.MachineLearning.name })
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
            VoiceAnalysisOption.MachineLearning
        ).test()
            .assertValue(false)

        timerTestScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        verify(messageController).sendMessage(argThat { voiceAnalysisOption == VoiceAnalysisOption.MachineLearning.name })
    }

    @Test
    fun `should save successful option change to database`() {
        voiceAnalysisUseCase.chooseVoiceAnalysisOption(
            messageController,
            VoiceAnalysisOption.MachineLearning
        ).test()
            .assertValue(true)

        verify(dataRepository).updateVoiceAnalysisOption(VoiceAnalysisOption.MachineLearning)
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
            VoiceAnalysisOption.MachineLearning
        ).test()
            .assertValue(false)

        verifyZeroInteractions(dataRepository)
    }
}
