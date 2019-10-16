package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import dagger.Reusable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class NotifyBabyCryingUseCase @Inject constructor(
    private val notificationSender: FirebaseNotificationSender
) {
    private val babyCryingEvents: PublishSubject<BabyCrying> = PublishSubject.create()

    private fun fetchClientsAndPostNotification(title: String, text: String) =
        notificationSender.broadcastNotificationToFcm(title, text)

    fun subscribe(title: String, text: String): Disposable =
        babyCryingEvents
            .throttleFirst(1, TimeUnit.MINUTES)
            .flatMapCompletable {
                fetchClientsAndPostNotification(title = title, text = text)
            }
            .subscribe()

    fun notifyBabyCrying() =
        babyCryingEvents.onNext(BabyCrying)

    private object BabyCrying
}
