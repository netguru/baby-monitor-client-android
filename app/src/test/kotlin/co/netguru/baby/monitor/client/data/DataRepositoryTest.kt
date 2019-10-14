package co.netguru.baby.monitor.client.data

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.client.ChildDataDao
import co.netguru.baby.monitor.client.data.client.home.log.LogDataDao
import co.netguru.baby.monitor.client.data.communication.ClientDataDao
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.data.splash.AppStateHandler
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DataRepositoryTest {

    @Rule
    @JvmField
    val schedulersRule = RxSchedulersOverrideRule()

    private lateinit var dataRepository: DataRepository
    private val database = mock<AppDatabase>()
    private val childDataDao = mock<ChildDataDao>()
    private val clientDataDao = mock<ClientDataDao>()
    private val logDataDao = mock<LogDataDao>()
    private val appStateHandler = mock<AppStateHandler>()

    @Before
    fun setup() {
        whenever(database.childDataDao()).thenReturn(childDataDao)
        whenever(database.clientDao()).thenReturn(clientDataDao)
        whenever(database.logDataDao()).thenReturn(logDataDao)
        dataRepository = DataRepository(database, appStateHandler)
    }

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
        whenever(childDataDao.getCount(address)).thenReturn(1)
        var isAdded = false
        dataRepository.doesChildDataExists(address).subscribe { doesExist ->
            isAdded = doesExist
        }
        assert(isAdded)
    }

    @Test
    fun `should return false if childData doesn't exist`() {
        val address = "123"
        whenever(childDataDao.getCount(address)).thenReturn(0)
        var isAdded = false
        dataRepository.doesChildDataExists(address).subscribe { doesExist ->
            isAdded = doesExist
        }
        assert(!isAdded)
    }
}
