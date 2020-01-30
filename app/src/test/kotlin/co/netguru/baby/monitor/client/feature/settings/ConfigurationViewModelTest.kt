package co.netguru.baby.monitor.client.feature.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisUseCase
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConfigurationViewModelTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val resetAppUseCase: ResetAppUseCase = mock()
    private val firebaseRepository: FirebaseRepository = mock()
    private val resetStateObserver: Observer<ChangeState> = mock()
    private val messageController: MessageController = mock()
    private val voiceAnalysisUseCase: VoiceAnalysisUseCase = mock()

    private val configurationViewModel = ConfigurationViewModel(
        resetAppUseCase,
        firebaseRepository,
        voiceAnalysisUseCase
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
}
