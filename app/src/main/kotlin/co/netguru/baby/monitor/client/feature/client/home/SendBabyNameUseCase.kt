package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.Completable
import timber.log.Timber
import javax.inject.Inject

class SendBabyNameUseCase @Inject constructor(
    private val dataRepository: DataRepository
) {
    fun streamBabyName(client: RxWebSocketClient): Completable =
        dataRepository.getChildData()
            .map { it.name.orEmpty() }
            .flatMapCompletable { name ->
                sendBabyName(client, name)
            }

    private fun sendBabyName(client: RxWebSocketClient, babyName: String): Completable =
        client.send(Message(babyName = babyName))
            .doOnError { Timber.w("Couldn't send baby name: $babyName.") }
            .doOnComplete { Timber.d("Baby name sent: $babyName.") }
}
