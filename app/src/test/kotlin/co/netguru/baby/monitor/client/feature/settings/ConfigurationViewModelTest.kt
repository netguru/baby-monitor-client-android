package co.netguru.baby.monitor.client.feature.settings

import android.app.Activity
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
    private val nsdServiceManager: NsdServiceManager = mock()
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
    private val nsdServiceInfo: NsdServiceInfo = mock {
        on { port }.doReturn(1)
        val inetAddress: InetAddress = mock {
            on { hostAddress }.doReturn("address")
        }
        on { host }.doReturn(inetAddress)
    }
    private val configCompletedObserver: Observer<Boolean> = mock()
    private val configurationViewModel = ConfigurationViewModel(
        nsdServiceManager,
        resetAppUseCase,
        dataRepository,
        firebaseRepository,
        localDateTimeProvider
    )

    @Before
    fun setUp() {
        configurationViewModel.connectionCompletedState.observeForever(configCompletedObserver)
    }

    @Test
    fun `should properly handle new service`() {
        whenever(dataRepository.putChildData(any())).doReturn(Completable.complete())
        whenever(dataRepository.insertLogToDatabase(any())).doReturn(Completable.complete())

        configurationViewModel.handleNewService(nsdServiceInfo)

        verify(dataRepository).putChildData(any())
        verify(dataRepository).insertLogToDatabase(any())
        verify(configCompletedObserver).onChanged(true)
    }

    @Test
    fun `should return false on config fail`() {
        whenever(dataRepository.doesChildDataExists(any())).doReturn(Single.error(Throwable()))
        whenever(dataRepository.putChildData(any())).doReturn(Completable.error(Throwable()))
        whenever(dataRepository.insertLogToDatabase(any())).doReturn(Completable.error(Throwable()))
        configurationViewModel.connectionCompletedState.observeForever(configCompletedObserver)

        configurationViewModel.handleNewService(nsdServiceInfo)

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
        configurationViewModel.discoverNsdService(wifiManager)

        verify(nsdServiceManager).discoverService()

        configurationViewModel.stopNsdServiceDiscovery()

        verify(nsdServiceManager).stopServiceDiscovery()
    }

    @Test
    fun `should handle Wifi MulticastLock while starting and stopping searching`() {
        configurationViewModel.discoverNsdService(wifiManager)

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
}
