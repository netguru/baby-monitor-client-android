package co.netguru.baby.monitor.client.feature.server

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import io.reactivex.Completable
import javax.inject.Inject

class ReceiveFirebaseTokenUseCase @Inject constructor(private val dataRepository: DataRepository) {
    fun receiveToken(ipAddress: String, token: String): Completable =
        dataRepository.insertClientData(ClientEntity(address = ipAddress, firebaseKey = token))
}
