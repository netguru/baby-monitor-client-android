package co.netguru.baby.monitor.client.feature.server

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ReceiveFirebaseTokenUseCase @Inject constructor(private val dataRepository: DataRepository) {
    fun receiveToken(ipAddress: String, token: String): Completable = Completable.fromAction {
        dataRepository.getAllClientData()
            .flatMapSingle { clientEntityList ->
                Single.just(clientEntityList
                    .firstOrNull { clientEntity ->
                        clientEntity.address == ipAddress || clientEntity.firebaseKey == token
                    }
                    ?: ClientEntity(
                        address = ipAddress,
                        firebaseKey = token
                    )
                )
            }
            .flatMapCompletable(dataRepository::insertClientData)
    }
}
