package co.netguru.baby.monitor.client.application.di

import android.content.Context
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.babynotification.NotifyBabyEventUseCase
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
object NotificationsModule {
    @Provides
    fun notificationHandler(context: Context) = NotificationHandler(context)

    @Provides
    @Reusable
    fun provideNotifyBabyEventUseCase(
        notificationSender: FirebaseNotificationSender,
        context: Context
    ) =
        NotifyBabyEventUseCase(
            notificationSender,
            mapOf(
                NotifyBabyEventUseCase.CRY_TITLE_KEY to context.resources.getString(R.string.notification_baby_is_crying_title),
                NotifyBabyEventUseCase.NOISE_TITLE_KEY to context.resources.getString(R.string.notification_noise_detected_title),
                NotifyBabyEventUseCase.NOTIFICATION_TEXT_KEY to context.resources.getString(R.string.notification_baby_is_crying_content)
            )
        )
}
