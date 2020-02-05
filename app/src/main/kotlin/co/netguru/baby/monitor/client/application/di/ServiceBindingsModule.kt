package co.netguru.baby.monitor.client.application.di

import co.netguru.baby.monitor.client.feature.babynotification.BabyMonitorMessagingService
import co.netguru.baby.monitor.client.feature.communication.webrtc.server.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.babynotification.BabyEventActionIntentService
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ServiceBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun bindVoiceAnalysisService(): VoiceAnalysisService

    @ContributesAndroidInjector
    internal abstract fun bindWebRtcService(): WebRtcService

    @ContributesAndroidInjector
    internal abstract fun bindWebSocketServerService(): WebSocketServerService

    @ContributesAndroidInjector
    internal abstract fun bindMessagingService(): BabyMonitorMessagingService

    @ContributesAndroidInjector
    internal abstract fun bindBabyEventActionIntentService(): BabyEventActionIntentService
}
