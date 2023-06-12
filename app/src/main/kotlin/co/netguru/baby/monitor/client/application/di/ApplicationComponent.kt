package co.netguru.baby.monitor.client.application.di

import android.app.Activity
import android.app.Service
import androidx.fragment.app.Fragment
import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.base.ViewModelModule
import co.netguru.baby.monitor.client.feature.babynotification.BabyEventActionIntentService
import co.netguru.baby.monitor.client.feature.babynotification.BabyMonitorMessagingService
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import co.netguru.baby.monitor.client.feature.client.home.log.ClientActivityLogFragment
import co.netguru.baby.monitor.client.feature.communication.pairing.PairingFragment
import co.netguru.baby.monitor.client.feature.communication.pairing.ServiceDiscoveryFragment
import co.netguru.baby.monitor.client.feature.communication.webrtc.server.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.onboarding.VoiceRecordingsSettingsFragment
import co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.FeaturePresentationFragment
import co.netguru.baby.monitor.client.feature.server.ChildMonitorFragment
import co.netguru.baby.monitor.client.feature.server.ServerActivity
import co.netguru.baby.monitor.client.feature.settings.ServerSettingsFragment
import co.netguru.baby.monitor.client.feature.splash.SplashFragment
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisService
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        ViewModelModule::class,
        ActivityBindingsModule::class,
        ServiceBindingsModule::class,
        SharedPreferencesModule::class,
        NotificationsModule::class,
        FirebaseModule::class,
        NetworkModule::class,
        LowBatteryModule::class
    ]
)
internal interface AppComponent {

    fun inject(app: App)

    fun inject(serverActivity: ServerActivity)
    fun inject(clientHomeActivity: ClientHomeActivity)

    fun inject(webSocketServerService: WebSocketServerService)
    fun inject(service: VoiceAnalysisService)
    fun inject(webRtcService: WebRtcService)
    fun inject(babyMonitorMessagingService: BabyMonitorMessagingService)
    fun inject(babyEventActionIntentService: BabyEventActionIntentService)

    fun inject(splashFragment: SplashFragment)
    fun inject(featurePresentationFragment: FeaturePresentationFragment)
    fun inject(baseFragment: BaseFragment)
    fun inject(childMonitorFragment: ChildMonitorFragment)
    fun inject(serverSettingsFragment: ServerSettingsFragment)
    fun inject(serviceDiscoveryFragment: ServiceDiscoveryFragment)
    fun inject(pairingFragment: PairingFragment)
    fun inject(voiceRecordingsSettingsFragment: VoiceRecordingsSettingsFragment)
    fun inject(clientDashboardFragment: ClientDashboardFragment)
    fun inject(clientActivityLogFragment: ClientActivityLogFragment)



    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: App): AppComponent
    }

    companion object {
        fun create(application: App) = DaggerAppComponent.factory()
            .create(application)

        val Activity.appComponent: AppComponent
            get() = (application as App).appComponent

        val Service.appComponent: AppComponent
            get() = (application as App).appComponent

        val Fragment.appComponent: AppComponent
            get() = requireActivity().appComponent
        val BaseFragment.appComponent: AppComponent
            get() = requireActivity().appComponent
    }
}
