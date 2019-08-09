package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import dagger.Reusable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

@Reusable
class NotifyBabyCryingUseCase @Inject constructor(private val dataRepository: DataRepository) {
    fun notifyBabyCrying() =
        dataRepository.getAllClientData()
            .firstOrError()
            .flattenAsObservable { it }
            .map(ClientEntity::firebaseKey)
            .subscribeBy(
                onNext = { token ->
                    Timber.d("Sending a message to $token.")
                    // TODO send baby crying notification
                },
                onComplete = {
                    Timber.d("Sending push messages completed.")
                },
                onError = { error ->
                    Timber.w(error, "Sending push messages error.")
                }
            )
}
