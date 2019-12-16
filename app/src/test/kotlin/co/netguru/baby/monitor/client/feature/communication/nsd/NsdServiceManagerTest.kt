package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager.Companion.SERVICE_NAME
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.InetAddress

class NsdServiceManagerTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val argumentCaptorDiscovery = argumentCaptor<NsdManager.DiscoveryListener>()
    private val argumentCaptorResolve = argumentCaptor<NsdManager.ResolveListener>()

    private val nsdManager = mock<NsdManager>()
    private val deviceNameProvider = mock<IDeviceNameProvider>()
    private val nsdServiceInfo: NsdServiceInfo = mock {
        on { port }.doReturn(1)
        on { serviceName }.doReturn("$SERVICE_NAME phone")
        val inetAddress: InetAddress = mock {
            on { hostAddress }.doReturn("address")
        }
        on { host }.doReturn(inetAddress)
    }
    private val nsdServiceManager = NsdServiceManager(nsdManager, deviceNameProvider)
    private val nsdStateObserver = mock<Observer<NsdState>>()

    @Before
    fun setUp() {
        nsdServiceManager.nsdStateLiveData.observeForever(nsdStateObserver)
        nsdServiceManager.stopServiceDiscovery()
    }

    @Test
    fun `should start discovery on NsdManager`() {
        nsdServiceManager.discoverService()

        verify(nsdManager).discoverServices(any(), any(), any())
    }

    @Test
    fun `should discover and resolve NsdService`() {
        nsdServiceManager.discoverService()

        verify(nsdManager).discoverServices(any(), any(), argumentCaptorDiscovery.capture())
        argumentCaptorDiscovery.firstValue.onServiceFound(nsdServiceInfo)

        verify(nsdManager).resolveService(eq(nsdServiceInfo), argumentCaptorResolve.capture())
        argumentCaptorResolve.firstValue.onServiceResolved(nsdServiceInfo)

        verify(nsdStateObserver).onChanged(argThat {
            this is NsdState.InProgress && serviceInfoList.contains(
                nsdServiceInfo
            )
        })
    }

    @Test
    fun `should stop service discovery`() {
        nsdServiceManager.discoverService()

        verify(nsdManager).discoverServices(any(), any(), argumentCaptorDiscovery.capture())
        argumentCaptorDiscovery.firstValue.onDiscoveryStopped(any())

        verify(nsdManager).stopServiceDiscovery(any())
    }

    @Test
    fun `should post error state on StartDiscoveryFailedException`() {
        nsdServiceManager.discoverService()

        verify(nsdManager).discoverServices(any(), any(), argumentCaptorDiscovery.capture())
        argumentCaptorDiscovery.firstValue.onStartDiscoveryFailed("", 0)

        verify(nsdStateObserver).onChanged(argThat {
            this is NsdState.Error
                    && throwable is StartDiscoveryFailedException
        })
    }

    @Test
    fun `should post error state on ResolveFailedException`() {
        nsdServiceManager.discoverService()

        verify(nsdManager).discoverServices(any(), any(), argumentCaptorDiscovery.capture())
        argumentCaptorDiscovery.firstValue.onServiceFound(nsdServiceInfo)

        verify(nsdManager).resolveService(eq(nsdServiceInfo), argumentCaptorResolve.capture())
        argumentCaptorResolve.firstValue.onResolveFailed(nsdServiceInfo, 0)

        verify(nsdStateObserver).onChanged(argThat {
            this is NsdState.Error
                    && throwable is ResolveFailedException
        })
    }
}
