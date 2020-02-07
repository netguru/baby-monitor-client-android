package co.netguru.baby.monitor.client.feature.communication

import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConfirmationUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val schedulersProvider: ISchedulersProvider
) {
    fun changeValue(
        messageController: MessageController,
        confirmationItem: ConfirmationItem<*>
    ): Single<Boolean> {
        return Completable.fromAction {
            messageController.sendMessage(confirmationItem.sentMessage)
        }
            .andThen(
                messageController.receivedMessages()
                    .filter { it.confirmationId == confirmationItem.sentMessage.confirmationId }
                    .timeout(RESPONSE_TIMEOUT, TimeUnit.SECONDS, schedulersProvider.io())
                    .firstOrError()
                    .map { true })
            .doOnSuccess { success ->
                if (success) confirmationItem.onSuccessAction(dataRepository)
                    .subscribeBy(
                        onComplete = { Timber.i("Value updated to ${confirmationItem.value}") })
            }
            .onErrorReturnItem(false)
    }

    companion object {
        private const val RESPONSE_TIMEOUT = 5L
        const val NUMBERS_OF_DIGITS_IN_ID = 4
    }
}

interface ConfirmationItem<T> {
    fun onSuccessAction(dataRepository: DataRepository): Completable
    val value: T
    val sentMessage: Message
}
