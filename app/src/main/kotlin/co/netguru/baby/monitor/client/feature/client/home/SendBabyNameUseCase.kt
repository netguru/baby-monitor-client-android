package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.common.proto.Message
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

class SendBabyNameUseCase @Inject constructor(
    private val repo: DataRepository,
    private val gson: Gson
) {
    fun streamBabyName(client: RxWebSocketClient) =
        repo.getChildData()
            .map { it.name.orEmpty() }
            .flatMapCompletable { name ->
                sendBabyName(client, name)
            }

    fun sendBabyName(client: RxWebSocketClient, babyName: String) =
        client.send(gson.toJson(Message(babyName = babyName)))
            .doOnError { Timber.w("Couldn't send baby name: $babyName.") }
            .doOnComplete { Timber.d("Baby name sent: $babyName.") }
}
