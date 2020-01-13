package co.netguru.baby.monitor.client.feature.babycrynotification

import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.navigation.NavDeepLinkBuilder
import co.netguru.baby.monitor.TestUtils
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class OpenCameraUseCaseTest {

    private val analyticsManager = mock<AnalyticsManager>()
    private val openCameraUseCase = OpenCameraUseCase(analyticsManager)
    private val taskStackBuilder = mock<TaskStackBuilder>()
    private val navDeepLinkBuilder =
        TestUtils.mockBuilder<NavDeepLinkBuilder, TaskStackBuilder>(taskStackBuilder)
    private val snoozeDialogArgument = mock<Bundle>()

    @Test
    fun `should open correct activities stack`() {
        openCameraUseCase.openLiveClientCamera(navDeepLinkBuilder, snoozeDialogArgument)
        TestUtils.verifyInOrder(navDeepLinkBuilder) {
            setComponentName(ClientHomeActivity::class.java)
            setGraph(R.navigation.client_home_nav_graph)
            setDestination(R.id.clientLiveCamera)
            setArguments(snoozeDialogArgument)
            createTaskStackBuilder()
        }
        verify(taskStackBuilder).startActivities()
    }

    @Test
    fun `should send openCameraEvent to firebase`() {
        openCameraUseCase.openLiveClientCamera(navDeepLinkBuilder, snoozeDialogArgument)
        verify(analyticsManager).logEvent(AnalyticsManager.NOTIFICATION_OPEN_CAMERA_EVENT)
    }
}
