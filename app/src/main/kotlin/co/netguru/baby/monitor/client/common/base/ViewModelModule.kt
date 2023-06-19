package co.netguru.baby.monitor.client.common.base

import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.common.Randomiser
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.babynotification.SnoozeNotificationUseCase
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.client.home.RestartAppUseCase
import co.netguru.baby.monitor.client.feature.client.home.SendBabyNameUseCase
import co.netguru.baby.monitor.client.feature.client.home.livecamera.ClientLiveCameraFragmentViewModel
import co.netguru.baby.monitor.client.feature.communication.ConfirmationUseCase
import co.netguru.baby.monitor.client.feature.communication.internet.CheckInternetConnectionUseCase
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.communication.pairing.PairingUseCase
import co.netguru.baby.monitor.client.feature.communication.pairing.PairingViewModel
import co.netguru.baby.monitor.client.feature.communication.pairing.ServiceDiscoveryViewModel
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClientController
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageParser
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import co.netguru.baby.monitor.client.feature.server.ChildMonitorViewModel
import co.netguru.baby.monitor.client.feature.server.ReceiveFirebaseTokenUseCase
import co.netguru.baby.monitor.client.feature.server.ServerViewModel
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import co.netguru.baby.monitor.client.feature.settings.ResetAppUseCase
import co.netguru.baby.monitor.client.feature.settings.SettingsViewModel
import co.netguru.baby.monitor.client.feature.splash.SplashViewModel
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisUseCase
import dagger.Lazy
import dagger.Module
import dagger.Provides


@Module
internal class ViewModelModule {

    @Provides
    fun provideClientHomeActivityViewModel(
        dataRepository: DataRepository,
        sendBabyNameUseCase: SendBabyNameUseCase,
        noozeNotificationUseCase: SnoozeNotificationUseCase,
        checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
        restartAppUseCase: RestartAppUseCase,
        rxWebSocketClient: RxWebSocketClient,
        voiceAnalysisUseCase: VoiceAnalysisUseCase,
        messageParser: MessageParser
    ): ClientHomeViewModel = ClientHomeViewModel(
        dataRepository,
        sendBabyNameUseCase,
        noozeNotificationUseCase,
        checkInternetConnectionUseCase,
        restartAppUseCase,
        rxWebSocketClient,
        voiceAnalysisUseCase,
        messageParser
    )

    @Provides
    fun provideClientLiveCameraFragmentViewModel(
        analyticsManager: AnalyticsManager,
        rtcClientController: RtcClientController
    ): ClientLiveCameraFragmentViewModel =
        ClientLiveCameraFragmentViewModel(analyticsManager, rtcClientController)

    @Provides
    fun provideConfigurationViewModel(
        resetAppUseCase: ResetAppUseCase,
        firebaseRepository: FirebaseRepository,
        onfirmationUseCase: ConfirmationUseCase,
        randomiser: Randomiser
    ): ConfigurationViewModel =
        ConfigurationViewModel(resetAppUseCase, firebaseRepository, onfirmationUseCase, randomiser)

    @Provides
    fun provideServerViewModel(
        nsdServiceManager: NsdServiceManager,
        dataRepository: DataRepository,
        receiveFirebaseTokenUseCase: Lazy<ReceiveFirebaseTokenUseCase>,
        schedulersProvider: ISchedulersProvider
    ): ServerViewModel = ServerViewModel(
        nsdServiceManager,
        dataRepository,
        receiveFirebaseTokenUseCase,
        schedulersProvider
    )

    @Provides
    fun provideSplashViewModel(dataRepository: DataRepository): SplashViewModel =
        SplashViewModel(dataRepository)

    @Provides
    fun provideSettingsViewModel(dataRepository: DataRepository): SettingsViewModel =
        SettingsViewModel(dataRepository)

    @Provides
    fun provideChildMonitorViewModel(
        notifyLowBatteryUseCase: NotifyLowBatteryUseCase,
        analyticsManager: AnalyticsManager
    ): ChildMonitorViewModel = ChildMonitorViewModel(notifyLowBatteryUseCase, analyticsManager)

    @Provides
    fun providePairingViewModel(
        pairingUseCase: PairingUseCase,
        randomiser: Randomiser
    ): PairingViewModel = PairingViewModel(pairingUseCase, randomiser)

    @Provides
    fun provideServiceDiscoveryViewModel(nsdServiceManager: NsdServiceManager): ServiceDiscoveryViewModel =
        ServiceDiscoveryViewModel(nsdServiceManager)
}
