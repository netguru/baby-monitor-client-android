package co.netguru.baby.monitor.client.data

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.client.ChildDataDao
import co.netguru.baby.monitor.client.data.client.home.log.LogDataDao
import co.netguru.baby.monitor.client.data.communication.ClientDataDao
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.data.splash.AppStateHandler
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
    private val dataRepository = DataRepository(database, appStateHandler)

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
}
