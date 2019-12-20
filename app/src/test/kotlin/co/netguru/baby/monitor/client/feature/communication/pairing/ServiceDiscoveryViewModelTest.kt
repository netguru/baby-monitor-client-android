package co.netguru.baby.monitor.client.feature.communication.pairing

import android.net.wifi.WifiManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test

class ServiceDiscoveryViewModelTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val nsdServiceManager: NsdServiceManager = mock()

    private val multicastLock: WifiManager.MulticastLock = mock()
    private val wifiManager: WifiManager = mock {
        on { createMulticastLock(ServiceDiscoveryViewModel.MUTLICAST_LOCK_TAG) }.doReturn(
            multicastLock
        )
    }

    private val serviceDiscoveryViewModel = ServiceDiscoveryViewModel(
        nsdServiceManager
    )

    @Test
    fun `should start and stop nsdService`() {
        serviceDiscoveryViewModel.discoverNsdService(wifiManager)

        verify(nsdServiceManager).discoverService()

        serviceDiscoveryViewModel.stopNsdServiceDiscovery()

        verify(nsdServiceManager).stopServiceDiscovery()
    }

    @Test
    fun `should handle Wifi MulticastLock while starting and stopping searching`() {
        serviceDiscoveryViewModel.discoverNsdService(wifiManager)

        verify(multicastLock).acquire()
        verify(multicastLock).setReferenceCounted(true)

        serviceDiscoveryViewModel.stopNsdServiceDiscovery()

        verify(multicastLock).release()
    }
}
