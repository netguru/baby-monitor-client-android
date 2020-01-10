package co.netguru.baby.monitor.client.feature.babycrynotification

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.babycrynotification.BabyMonitorMessagingService.Companion.CRYING_NOTIFICATION_ID
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class CryingActionIntentService : IntentService(NAME) {

    @Inject
    internal lateinit var snoozeNotificationUseCase: SnoozeNotificationUseCase
    @Inject
    internal lateinit var openCameraUseCase: OpenCameraUseCase
    private val disposables = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        NotificationManagerCompat.from(this).cancel(CRYING_NOTIFICATION_ID)

        when (intent?.action) {
            NotificationHandler.SHOW_CAMERA_ACTION -> openCameraUseCase
                .openLiveClientCamera(NavDeepLinkBuilder(this))
            NotificationHandler.SNOOZE_ACTION -> snoozeNotificationUseCase
                .snoozeNotifications()
                .addTo(disposables)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    companion object {
        private const val NAME = "CRYING_ACTION_INTENT_SERVICE"
        const val SHOULD_SHOW_SNOOZE_DIALOG = "SHOULD_SHOW_SNOOZE_DIALOG"
    }
}
