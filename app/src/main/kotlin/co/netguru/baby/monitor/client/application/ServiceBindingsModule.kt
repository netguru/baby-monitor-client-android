package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.feature.babycrynotification.BabyMonitorMessagingService
import co.netguru.baby.monitor.client.feature.communication.webrtc.server.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.babycrynotification.CryingActionIntentService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ServiceBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun bindMachineLearningService(): MachineLearningService

    @ContributesAndroidInjector
    internal abstract fun bindWebRtcService(): WebRtcService

    @ContributesAndroidInjector
    internal abstract fun bindWebSocketServerService(): WebSocketServerService

    @ContributesAndroidInjector
    internal abstract fun bindMessagingService(): BabyMonitorMessagingService

    @ContributesAndroidInjector
    internal abstract fun bindSnoozeActionService(): CryingActionIntentService
}
