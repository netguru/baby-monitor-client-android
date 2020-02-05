package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.common.Randomiser
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VoiceAnalysisUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val schedulersProvider: ISchedulersProvider,
    private val randomiser: Randomiser
) {
    fun chooseVoiceAnalysisOption(
        messageController: MessageController,
        voiceAnalysisOption: VoiceAnalysisOption
    ): Single<Boolean> {
        val sentMessage = Message(
            voiceAnalysisOption = voiceAnalysisOption.name,
            confirmationId = randomiser.getRandomDigits(NUMBERS_OF_DIGITS_IN_ID).joinToString("")
        )
        return Completable.fromAction {
            messageController.sendMessage(sentMessage)
        }
            .andThen(
                messageController.receivedMessages()
                    .filter { it.confirmationId == sentMessage.confirmationId }
                    .timeout(RESPONSE_TIMEOUT, TimeUnit.SECONDS, schedulersProvider.io())
                    .firstOrError()
                    .map { true })
            .doOnSuccess { success ->
                if (success) dataRepository.updateVoiceAnalysisOption(voiceAnalysisOption)
                    .subscribeBy(
                        onComplete = { Timber.i("VoiceOption updated to $voiceAnalysisOption") })
            }
            .onErrorReturnItem(false)
    }

    companion object {
        private const val RESPONSE_TIMEOUT = 5L
        private const val NUMBERS_OF_DIGITS_IN_ID = 4
    }
}
