package co.netguru.baby.monitor.client.feature.client.home

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import co.netguru.baby.monitor.client.feature.common.FileManager
import co.netguru.baby.monitor.client.feature.common.extensions.toJson
import co.netguru.baby.monitor.client.feature.communication.webrtc.MainService
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcClient
import co.netguru.baby.monitor.client.feature.communication.websocket.*
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import com.nhaarman.mockito_kotlin.any
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import org.junit.Assert.*
import org.mockito.ArgumentMatchers
import org.webrtc.SurfaceViewRenderer

class ClientHomeViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var configurationRepository: ConfigurationRepository

    @Mock
    private lateinit var clientsHandler: ClientsHandler

    @Mock
    private lateinit var fileManager: FileManager

    private lateinit var viewModel: ClientHomeViewModel

    private val testScheduler = TestScheduler()

    private val childList = listOf(
            ChildData("test_address_1", "test_image_1", "test_name_1"),
            ChildData("test_address_2", "test_image_2", "test_name_2"),
            ChildData("test_address_3", "test_image_3", "test_name_3"),
            ChildData("test_address_4", "test_image_4", "test_name_4")
    )

    @Before
    public fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { scheduler -> testScheduler };
        RxJavaPlugins.setComputationSchedulerHandler { scheduler -> testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { scheduler -> testScheduler }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler -> testScheduler }

        MockitoAnnotations.initMocks(this)

        `when`(configurationRepository.childrenList).thenReturn(childList)
        `when`(clientsHandler.addClient(ArgumentMatchers.anyString()))
                .thenAnswer { Single.just(it.arguments[0]) }
        `when`(fileManager.saveFile(any(), any())).then {
            val function = it.arguments[1] as ((String) -> Unit)
            function.invoke("test_path")

            return@then Single.just(true)
        }

        viewModel = ClientHomeViewModel(configurationRepository, clientsHandler, fileManager)
    }

    @Test
    fun `Initialised view model should not have list of children`() {
        val observer = mock<Observer<List<ChildData>>>()
        viewModel.childList.observeForever(observer)

        verifyZeroInteractions(observer)
    }

    @Test
    fun `Initialised view model should have current child marked as null`() {
        val observer = mock<Observer<ChildData>>()
        viewModel.selectedChild.observeForever(observer)

        verifyZeroInteractions(observer)
    }

    @Test
    fun `Refreshing child list should populate child list and current child`() {
        val observer = mock<Observer<List<ChildData>>>()
        viewModel.childList.observeForever(observer)

        verifyZeroInteractions(observer)

        viewModel.refreshChildrenList()
        testScheduler.triggerActions()

        verify(observer).onChanged(childList)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `Refreshing child list should populate current child with first retrieved child`() {
        val observer = mock<Observer<ChildData>>()
        viewModel.selectedChild.observeForever(observer)
        verifyZeroInteractions(observer)

        viewModel.refreshChildrenList()
        testScheduler.triggerActions()

        verify(observer).onChanged(childList[0])
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `Refreshing child list should open connections for all addresses`() {
        viewModel.refreshChildrenList()
        testScheduler.triggerActions()

        for (childData in childList) {
            verify(clientsHandler).addClient(childData.address)
        }
    }

    @Test
    fun `Updating child name should update it in repository`() {
        viewModel.selectedChild.value = childList[0]

        assert(viewModel.selectedChild.value!!.name == childList[0].name)

        viewModel.updateChildName("updated_name")
        testScheduler.triggerActions()

        assertEquals("updated_name", childList[0].name)
        verify(configurationRepository).updateChildData(childList[0])
    }

    @Test
    fun `Updating child name should trigger observer with current selected child`() {
        viewModel.selectedChild.value = childList[0]
        assert(viewModel.selectedChild.value!!.name == childList[0].name)

        val observer = mock<Observer<ChildData>>()
        viewModel.selectedChild.observeForever(observer)
        verify(observer).onChanged(childList[0])

        clearInvocations(observer)

        viewModel.updateChildName("updated_name")
        testScheduler.triggerActions()

        assertEquals("updated_name", childList[0].name)
        verify(observer).onChanged(childList[0])
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `Saving image should delete previous file if exists`() {
        viewModel.saveImage(mock())
        verify(fileManager).deleteFileIfExists(any())
    }

    @Test
    fun `Saving image should save it to disk`() {
        viewModel.saveImage(mock())
        verify(fileManager).saveFile(any(), any())
    }

    @Test
    fun `Saving image should save it to repository`() {
        viewModel.selectedChild.value = childList[0]

        viewModel.saveImage(mock())
        testScheduler.triggerActions()

        assertEquals("test_path", childList[0].image)
        verify(configurationRepository).updateChildData(childList[0])
    }

    @Test
    fun `Saving image should trigger observer with current child`() {
        viewModel.selectedChild.value = childList[0]

        val observer = mock<Observer<ChildData>>()
        viewModel.selectedChild.observeForever(observer)
        verify(observer).onChanged(childList[0])

        clearInvocations(observer)

        viewModel.saveImage(mock())
        testScheduler.triggerActions()

        assertEquals("test_path", childList[0].image)
        verify(observer).onChanged(childList[0])
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun `When replying lullaby proper action should be passed to websocket client`() {
        val lullabyName = "lullaby_name"
        viewModel.lullabyCommand.value = LullabyCommand(lullabyName, Action.STOP)
        viewModel.selectedChild.value = childList[0]

        val mockClient = mock<CustomWebSocketClient>()
        `when`(clientsHandler.getClient(any())).thenReturn(mockClient)

        viewModel.repeatLullaby()
        testScheduler.triggerActions()

        val expectedCommand = LullabyCommand(lullabyName, Action.REPEAT)
        verify(mockClient).sendMessage(expectedCommand.toJson())
    }

    @Test
    fun `When stopping lullaby proper action should be passed to websocket client`() {
        val lullabyName = "lullaby_name"
        viewModel.lullabyCommand.value = LullabyCommand(lullabyName, Action.PLAY)
        viewModel.selectedChild.value = childList[0]

        val mockClient = mock<CustomWebSocketClient>()
        `when`(clientsHandler.getClient(any())).thenReturn(mockClient)

        viewModel.stopPlayback()
        testScheduler.triggerActions()

        val expectedCommand = LullabyCommand(lullabyName, Action.STOP)
        verify(mockClient).sendMessage(expectedCommand.toJson())
    }

    @Test
    fun `When lullaby is stopped, switching playback should resume it`() {
        val lullabyName = "lullaby_name"
        viewModel.lullabyCommand.value = LullabyCommand(lullabyName, Action.STOP)
        viewModel.selectedChild.value = childList[0]

        val mockClient = mock<CustomWebSocketClient>()
        `when`(clientsHandler.getClient(any())).thenReturn(mockClient)

        viewModel.switchPlayback()
        testScheduler.triggerActions()

        val expectedCommand = LullabyCommand(lullabyName, Action.RESUME)
        verify(mockClient).sendMessage(expectedCommand.toJson())
    }

    @Test
    fun `When lullaby is playing, switching playback should stop it`() {
        val lullabyName = "lullaby_name"
        viewModel.lullabyCommand.value = LullabyCommand(lullabyName, Action.PLAY)
        viewModel.selectedChild.value = childList[0]

        val mockClient = mock<CustomWebSocketClient>()
        `when`(clientsHandler.getClient(any())).thenReturn(mockClient)

        viewModel.switchPlayback()
        testScheduler.triggerActions()

        val expectedCommand = LullabyCommand(lullabyName, Action.PAUSE)
        verify(mockClient).sendMessage(expectedCommand.toJson())
    }

    @Test
    fun `Selecting child by address should trigger selected child observer`() {
        val observer = mock<Observer<ChildData>>()
        viewModel.selectedChild.observeForever(observer)

        verifyZeroInteractions(observer)

        viewModel.setSelectedChildWithAddress("test_address_3")
        testScheduler.triggerActions()

        verify(observer).onChanged(childList[2])
    }

    @Test
    fun `Selecting child by address should refresh child list`() {
        val observer = mock<Observer<List<ChildData>>>()
        viewModel.childList.observeForever(observer)
        verifyZeroInteractions(observer)

        viewModel.setSelectedChildWithAddress("test_address_3")
        testScheduler.triggerActions()

        verify(observer).onChanged(childList)
    }

    @Test
    fun `When selecting child by address which does not exist no action should be taken`() {
        val childListObserver = mock<Observer<List<ChildData>>>()
        val childObserver = mock<Observer<ChildData>>()

        viewModel.childList.observeForever(childListObserver)
        viewModel.selectedChild.observeForever(childObserver)

        viewModel.setSelectedChildWithAddress("no_existent_address")
        testScheduler.triggerActions()

        verifyZeroInteractions(childListObserver)
        verifyZeroInteractions(childObserver)
    }

    @Test
    fun `Hunging up call should invoke proper method on call`() {
        val mockRtcClient = mock<RtcClient>()
        viewModel.currentCall = mockRtcClient
        viewModel.hangUp()

        verify(mockRtcClient).hangUp()
    }

    @Test
    fun `Remote renderer should be set RTC client`() {
        val mockRtcClient = mock<RtcClient>()
        viewModel.currentCall = mockRtcClient

        val remoteRendererMock = mock<SurfaceViewRenderer>()
        viewModel.setRemoteRenderer(remoteRendererMock)

        verify(mockRtcClient).remoteRenderer = remoteRendererMock
    }

    @Test
    fun `Starting call should set current call field`() {
        viewModel.selectedChild.value = childList[0]

        assertNull(viewModel.currentCall)
        `when`(clientsHandler.getClient(any())).thenReturn(mock())

        val mockRtcClient = mock<RtcClient>()
        val binderMock = mock<MainService.MainBinder>()
        `when`(binderMock.createClient(any())).thenReturn(mockRtcClient)

        viewModel.startCall(binderMock, mock(), mock())
        testScheduler.triggerActions()

        assertEquals(mockRtcClient, viewModel.currentCall)
    }

    @Test
    fun `Starting call should start real call`() {
        viewModel.selectedChild.value = childList[0]

        assertNull(viewModel.currentCall)
        `when`(clientsHandler.getClient(any())).thenReturn(mock())

        val mockRtcClient = mock<RtcClient>()
        val binderMock = mock<MainService.MainBinder>()
        `when`(binderMock.createClient(any())).thenReturn(mockRtcClient)

        viewModel.startCall(binderMock, mock(), mock())
        testScheduler.triggerActions()

        verify(mockRtcClient).startCall(any(), any())
    }

    @Test
    fun `When child does not have provided image data should not be marked as filled`() {
        val child = ChildData("address", null, "name")
        viewModel.selectedChild.value = child

        val isFilled = viewModel.isBabyDataFilled()
        assertFalse(isFilled)
    }

    @Test
    fun `When current child is not set, baby data should not be marked as filled`() {
        assertEquals(null, viewModel.selectedChild.value)

        val isFilled = viewModel.isBabyDataFilled()
        assertFalse(isFilled)
    }

    @Test
    fun `When user has image, baby data should be marked as filed`() {
        viewModel.selectedChild.value = childList[0]

        val isFilled = viewModel.isBabyDataFilled()
        assertTrue(isFilled)
    }
}
