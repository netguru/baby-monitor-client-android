package co.netguru.baby.monitor.client.application

import android.app.Application
import javax.inject.Inject

class BuildTypeModule @Inject constructor(private val debugNotificationManager: DebugNotificationManager) {
    fun initialize(app: Application) {
        debugNotificationManager.show(app)
    }
}
