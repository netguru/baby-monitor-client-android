package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.Completable
import timber.log.Timber
import javax.inject.Inject

class VoiceAnalysisUseCase @Inject constructor(
    private val dataRepository: DataRepository
    ) {
    fun sendInitialVoiceAnalysisOption(client: RxWebSocketClient): Completable =
        dataRepository.getChildData()
            .map { it.voiceAnalysisOption }
            .flatMapCompletable { voiceAnalysisOption ->
                sendVoiceAnalysisOption(client, voiceAnalysisOption)
            }

    private fun sendVoiceAnalysisOption(
        client: RxWebSocketClient,
        voiceAnalysisOption: VoiceAnalysisOption
    ): Completable =
        client.send(Message(voiceAnalysisOption = voiceAnalysisOption.name))
            .doOnError { Timber.w("Couldn't send option: $voiceAnalysisOption.") }
            .doOnComplete { Timber.d("Option sent: $voiceAnalysisOption.") }
}
