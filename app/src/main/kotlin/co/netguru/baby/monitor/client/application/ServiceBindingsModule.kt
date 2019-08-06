package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.webrtc.receiver.WebRtcReceiverService
import co.netguru.baby.monitor.client.feature.communication.websocket.ClientHandlerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ServiceBindingsModule {

    @ContributesAndroidInjector
    internal abstract fun ClientHandlerServiceInjector(): ClientHandlerService

    @ContributesAndroidInjector
    internal abstract fun WebRtcReceiverServiceInjector(): WebRtcReceiverService

    @ContributesAndroidInjector
    internal abstract fun WebRtcService(): WebRtcService
}
