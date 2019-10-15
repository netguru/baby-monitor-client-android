package co.netguru.baby.monitor.client.feature.babycrynotification

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.babycrynotification.BabyMonitorMessagingService.Companion.CRYING_NOTIFICATION_ID
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CryingActionIntentService : IntentService(NAME) {

    @Inject
    internal lateinit var dataRepository: DataRepository
    private val disposables = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        NotificationManagerCompat.from(this).cancel(CRYING_NOTIFICATION_ID)

        when (intent?.action) {
            NotificationHandler.SHOW_CAMERA_ACTION -> handleShowCameraAction()
            NotificationHandler.SNOOZE_ACTION -> handleSnoozeAction()
        }
    }

    private fun handleShowCameraAction() {
        openClientLiveCamera()
    }

    private fun openClientLiveCamera() {
        NavDeepLinkBuilder(this)
            .setComponentName(ClientHomeActivity::class.java)
            .setGraph(R.navigation.client_home_nav_graph)
            .setDestination(R.id.clientLiveCamera)
            .createTaskStackBuilder()
            .startActivities()
    }

    private fun handleSnoozeAction() {
        dataRepository.updateChildSnoozeTimestamp(
            System.currentTimeMillis() + FIVE_MINUTES_SNOOZE_TIME
        )
            .subscribe { Timber.i("Notification snooze timestamp updated") }
            .addTo(disposables)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    companion object {
        private const val NAME = "CRYING_ACTION_INTENT_SERVICE"
        private val FIVE_MINUTES_SNOOZE_TIME = TimeUnit.MINUTES.toMillis(5)
    }
}
