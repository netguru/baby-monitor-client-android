package co.netguru.baby.monitor.client.application.di

import android.content.Context
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.babycrynotification.NotifyBabyCryingUseCase
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
    fun provideNotifyBabyCryingUseCase(
        notificationSender: FirebaseNotificationSender,
        context: Context
    ) =
        NotifyBabyCryingUseCase(
            notificationSender,
            context.resources.getString(R.string.notification_baby_is_crying_title),
            context.resources.getString(R.string.notification_baby_is_crying_content)
        )
}
