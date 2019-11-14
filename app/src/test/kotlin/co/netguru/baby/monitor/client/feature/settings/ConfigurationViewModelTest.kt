package co.netguru.baby.monitor.client.feature.settings

import android.app.Activity
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.LocalDateTimeProvider
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.net.InetAddress

class ConfigurationViewModelTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val resetAppUseCase: ResetAppUseCase = mock()
    private val dataRepository: DataRepository = mock()
    private val firebaseRepository: FirebaseRepository = mock()
    private lateinit var configurationViewModel: ConfigurationViewModel
    private val serviceInfoData = MutableLiveData<List<NsdServiceInfo>>()
    private val nsdServiceManager: NsdServiceManager = mock {
        on { serviceInfoData }.doReturn(serviceInfoData)
    }
    private val localDateTimeProvider: LocalDateTimeProvider = mock {
        val localDateTime: LocalDateTime = mock()
        on { now() }.doReturn(localDateTime)
    }
    private val multicastLock: WifiManager.MulticastLock = mock()
    private val wifiManager: WifiManager = mock {
        on { createMulticastLock(ConfigurationViewModel.MUTLICAST_LOCK_TAG) }.doReturn(
            multicastLock
        )
    }

    @Before
    fun setup() {
        configurationViewModel = ConfigurationViewModel(
            nsdServiceManager,
            resetAppUseCase,
            dataRepository,
            firebaseRepository,
            localDateTimeProvider
        )
    }

    @Test
    fun `should properly handle new service`() {
        val configCompletedObserver: Observer<Boolean> = mock()
        val serviceList = prepareServicesList()
        whenever(dataRepository.putChildData(any())).doReturn(Completable.complete())
        whenever(dataRepository.insertLogToDatabase(any())).doReturn(Completable.complete())
        configurationViewModel.connectionCompletedState.observeForever(configCompletedObserver)

        serviceInfoData.postValue(serviceList)

        verify(dataRepository).putChildData(any())
        verify(dataRepository).insertLogToDatabase(any())
        verify(configCompletedObserver).onChanged(true)
    }

    @Test
    fun `should return false on config fail`() {
        val configCompletedObserver: Observer<Boolean> = mock()
        val serviceList = prepareServicesList()
        whenever(dataRepository.doesChildDataExists(any())).doReturn(Single.error(Throwable()))
        whenever(dataRepository.putChildData(any())).doReturn(Completable.error(Throwable()))
        whenever(dataRepository.insertLogToDatabase(any())).doReturn(Completable.error(Throwable()))
        configurationViewModel.connectionCompletedState.observeForever(configCompletedObserver)

        serviceInfoData.postValue(serviceList)

        verify(configCompletedObserver).onChanged(false)
    }

    @Test
    fun `should handle app reset`() {
        val activity: Activity = mock()
        whenever(resetAppUseCase.resetApp()).doReturn(Completable.complete())

        configurationViewModel.resetApp(activity)

        verify(resetAppUseCase).resetApp()
        verify(activity).startActivity(any())
        verify(activity).finish()
    }

    @Test
    fun `should start and stop nsdService`() {
        val serviceConnectedListener: NsdServiceManager.OnServiceConnectedListener = mock()
        configurationViewModel.discoverNsdService(serviceConnectedListener, wifiManager)

        verify(nsdServiceManager).discoverService(serviceConnectedListener)

        configurationViewModel.stopNsdServiceDiscovery()

        verify(nsdServiceManager).stopServiceDiscovery()
    }

    @Test
    fun `should handle Wifi MulticastLock while starting and stopping searching`() {
        val serviceConnectedListener: NsdServiceManager.OnServiceConnectedListener = mock()
        configurationViewModel.discoverNsdService(serviceConnectedListener, wifiManager)

        verify(multicastLock).acquire()
        verify(multicastLock).setReferenceCounted(true)

        configurationViewModel.stopNsdServiceDiscovery()

        verify(multicastLock).release()
    }

    @Test
    fun `should handle upload settings using firebaseRepository`() {
        configurationViewModel.setUploadEnabled(true)

        verify(firebaseRepository).setUploadEnabled(true)

        configurationViewModel.isUploadEnabled()

        verify(firebaseRepository).isUploadEnablad()
    }

    private fun prepareServicesList(): List<NsdServiceInfo> {
        val nsdServiceInfo: NsdServiceInfo = mock {
            on { port }.doReturn(1)
            val inetAddress: InetAddress = mock {
                on { hostAddress }.doReturn("address")
            }
            on { host }.doReturn(inetAddress)
        }
        return listOf(nsdServiceInfo)
    }
}
