package co.netguru.baby.monitor.client.data

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.client.ChildDataDao
import co.netguru.baby.monitor.client.data.client.home.log.LogDataDao
import co.netguru.baby.monitor.client.data.communication.ClientDataDao
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.data.splash.AppStateHandler
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.UserProperty
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import com.nhaarman.mockitokotlin2.*
import org.junit.Rule
import org.junit.Test

class DataRepositoryTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val childDataDao = mock<ChildDataDao>()
    private val clientDataDao = mock<ClientDataDao>()
    private val logDataDao = mock<LogDataDao>()
    private val database = mock<AppDatabase> {
        on { childDataDao() }.doReturn(childDataDao)
        on { clientDao() }.doReturn(clientDataDao)
        on { logDataDao() }.doReturn(logDataDao)
    }
    private val appStateHandler = mock<AppStateHandler>()
    private val analyticsManager = mock<AnalyticsManager>()
    private val dataRepository = DataRepository(database, appStateHandler, analyticsManager)

    @Test
    fun `should delete data from all databases on deleteAllData`() {
        dataRepository.deleteAllData().subscribe()

        verify(childDataDao).deleteAll()
        verify(clientDataDao).deleteAll()
        verify(logDataDao).deleteAll()
    }

    @Test
    fun `should set app state undefined on deleteAllData`() {
        dataRepository.deleteAllData().subscribe()

        verify(appStateHandler).appState = AppState.UNDEFINED
    }

    @Test
    fun `should return true if childData exists`() {
        val address = "123"
        whenever(childDataDao.getCount(address)).doReturn(1)
        var isAdded = false
        dataRepository.doesChildDataExists(address).subscribe { doesExist ->
            isAdded = doesExist
        }
        assert(isAdded)
    }

    @Test
    fun `should return false if childData doesn't exist`() {
        val address = "123"
        whenever(childDataDao.getCount(address)).doReturn(0)
        var isAdded = false
        dataRepository.doesChildDataExists(address).subscribe { doesExist ->
            isAdded = doesExist
        }
        assert(!isAdded)
    }

    @Test
    fun `should update user property when saving AppState`() {
        val configuration = AppState.SERVER
        dataRepository.saveConfiguration(configuration)
            .test()
            .assertComplete()
        verify(analyticsManager).setUserProperty(argThat {
            this is UserProperty.AppStateProperty && this.value == configuration.name.toLowerCase()
        })
    }

    @Test
    fun `should update user property when saving VoiceAnalysisOption in server state`() {
        whenever(appStateHandler.appState).doReturn(AppState.SERVER)
        val voiceAnalysis = VoiceAnalysisOption.NOISE_DETECTION
        dataRepository.updateVoiceAnalysisOption(voiceAnalysis)
            .test()
            .assertComplete()

        verify(analyticsManager).setUserProperty(argThat {
            this is UserProperty.VoiceAnalysis && this.value == voiceAnalysis.name.toLowerCase()
        })
    }

    @Test
    fun `shouldn't update user property when saving VoiceAnalysisOption in client state`() {
        whenever(appStateHandler.appState).doReturn(AppState.CLIENT)
        val voiceAnalysis = VoiceAnalysisOption.NOISE_DETECTION
        dataRepository.updateVoiceAnalysisOption(voiceAnalysis)
            .test()
            .assertComplete()

        verifyNoMoreInteractions(analyticsManager)
    }

    @Test
    fun `should update user property when saving noise noiseLevel in server state`() {
        whenever(appStateHandler.appState).doReturn(AppState.SERVER)
        val noiseLevel = 30
        dataRepository.updateNoiseLevel(noiseLevel)
            .test()
            .assertComplete()

        verify(analyticsManager).setUserProperty(argThat {
            this is UserProperty.NoiseLevel && this.value == noiseLevel.toString()
        })
    }

    @Test
    fun `shouldn't update user property when saving noise noiseLevel in client state`() {
        whenever(appStateHandler.appState).doReturn(AppState.CLIENT)
        val noiseLevel = 30
        dataRepository.updateNoiseLevel(noiseLevel)
            .test()
            .assertComplete()

        verifyNoMoreInteractions(analyticsManager)
    }
}
