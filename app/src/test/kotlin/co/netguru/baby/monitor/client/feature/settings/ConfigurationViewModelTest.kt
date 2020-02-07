package co.netguru.baby.monitor.client.feature.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.Randomiser
import co.netguru.baby.monitor.client.feature.communication.ConfirmationUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConfigurationViewModelTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val resetAppUseCase = mock<ResetAppUseCase>()
    private val firebaseRepository = mock<FirebaseRepository>()
    private val resetStateObserver = mock<Observer<ChangeState>>()
    private val voiceAnalysisOptionObserver =
        mock<Observer<Pair<ChangeState, VoiceAnalysisOption?>>>()
    private val noiseSensitivityObserver =
        mock<Observer<Pair<ChangeState, Int?>>>()
    private val messageController = mock<MessageController>()
    private val confirmationUseCase = mock<ConfirmationUseCase>()
    private val randomiser = mock<Randomiser>()

    private val configurationViewModel = ConfigurationViewModel(
        resetAppUseCase,
        firebaseRepository,
        confirmationUseCase,
        randomiser
    )

    @Before
    fun setUp() {
        configurationViewModel.resetState.observeForever(resetStateObserver)
    }

    @Test
    fun `should complete app reset`() {
        whenever(resetAppUseCase.resetApp()).doReturn(Completable.complete())

        configurationViewModel.resetApp()

        verify(resetAppUseCase).resetApp()
        verify(resetStateObserver).onChanged(ChangeState.InProgress)
        verify(resetStateObserver).onChanged(ChangeState.Completed)
    }

    @Test
    fun `should complete app reset with messageSender`() {
        whenever(resetAppUseCase.resetApp(messageController)).doReturn(Completable.complete())

        configurationViewModel.resetApp(messageController)

        verify(resetAppUseCase).resetApp(messageController)
        verify(resetStateObserver).onChanged(ChangeState.InProgress)
        verify(resetStateObserver).onChanged(ChangeState.Completed)
    }

    @Test
    fun `should fail app reset`() {
        whenever(resetAppUseCase.resetApp()).doReturn(Completable.error(Throwable()))

        configurationViewModel.resetApp()

        verify(resetAppUseCase).resetApp()
        verify(resetStateObserver).onChanged(ChangeState.InProgress)
        verify(resetStateObserver).onChanged(ChangeState.Failed)
    }

    @Test
    fun `should handle upload settings using firebaseRepository`() {
        configurationViewModel.setUploadEnabled(true)

        verify(firebaseRepository).setUploadEnabled(true)

        configurationViewModel.isUploadEnabled()

        verify(firebaseRepository).isUploadEnablad()
    }

    @Test
    fun `should post completed state after successful voice analysis option change`() {
        val chosenOption = VoiceAnalysisOption.MACHINE_LEARNING
        whenever(
            confirmationUseCase.changeValue(eq(messageController), any())
        )
            .doReturn(Single.just(true))
        configurationViewModel.voiceAnalysisOptionState.observeForever(voiceAnalysisOptionObserver)

        configurationViewModel.chooseVoiceAnalysisOption(
            messageController,
            chosenOption
        )

        verify(voiceAnalysisOptionObserver).onChanged(argThat {
            first == ChangeState.InProgress && second == null
        })
        verify(voiceAnalysisOptionObserver).onChanged(argThat {
            first == ChangeState.Completed && second == chosenOption
        })
    }

    @Test
    fun `should post failed state with previous option`() {
        val previousOption = VoiceAnalysisOption.NOISE_DETECTION
        val chosenOption = VoiceAnalysisOption.MACHINE_LEARNING
        whenever(
            confirmationUseCase.changeValue(eq(messageController), any())
        )
            .doReturn(Single.just(false))
        configurationViewModel.voiceAnalysisOptionState.observeForever(voiceAnalysisOptionObserver)

        configurationViewModel.chooseVoiceAnalysisOption(
            messageController,
            chosenOption
        )

        verify(voiceAnalysisOptionObserver).onChanged(argThat {
            first == ChangeState.InProgress && second == null
        })
        verify(voiceAnalysisOptionObserver).onChanged(argThat {
            first == ChangeState.Failed && second == previousOption
        })
    }

    @Test
    fun `should post completed state after successful noise sensitivity change`() {
        val chosenSensitivity = 50
        whenever(
            confirmationUseCase.changeValue(eq(messageController), any())
        )
            .doReturn(Single.just(true))
        configurationViewModel.noiseSensitivityState.observeForever(noiseSensitivityObserver)

        configurationViewModel.changeNoiseSensitivity(
            messageController,
            chosenSensitivity
        )

        verify(noiseSensitivityObserver).onChanged(argThat {
            first == ChangeState.InProgress && second == null
        })
        verify(noiseSensitivityObserver).onChanged(argThat {
            first == ChangeState.Completed && second == chosenSensitivity
        })
    }

    @Test
    fun `should post failed state with previous noise sensitivity`() {
        val chosenSensitivity = 50
        whenever(
            confirmationUseCase.changeValue(eq(messageController), any())
        )
            .doReturn(Single.just(false))
        configurationViewModel.noiseSensitivityState.observeForever(noiseSensitivityObserver)

        configurationViewModel.changeNoiseSensitivity(
            messageController,
            chosenSensitivity
        )

        verify(noiseSensitivityObserver).onChanged(argThat {
            first == ChangeState.InProgress && second == null
        })
        verify(noiseSensitivityObserver).onChanged(argThat {
            first == ChangeState.Failed && second == configurationViewModel.noiseSensitivityInitialValue
        })
    }
}
