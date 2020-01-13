package co.netguru.baby.monitor.client.feature.babycrynotification

import android.os.Bundle
import androidx.navigation.NavDeepLinkBuilder
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.Event
import co.netguru.baby.monitor.client.feature.analytics.EventType
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import javax.inject.Inject

class OpenCameraUseCase @Inject constructor(
    private val analyticsManager: AnalyticsManager
) {
    fun openLiveClientCamera(navDeepLinkBuilder: NavDeepLinkBuilder, snoozeDialogArgument: Bundle) {
        analyticsManager.logEvent(Event.Simple(EventType.NOTIFICATION_OPEN_CAMERA))
        navDeepLinkBuilder
            .setComponentName(ClientHomeActivity::class.java)
            .setGraph(R.navigation.client_home_nav_graph)
            .setDestination(R.id.clientLiveCamera)
            .setArguments(snoozeDialogArgument)
            .createTaskStackBuilder()
            .startActivities()
    }
}
