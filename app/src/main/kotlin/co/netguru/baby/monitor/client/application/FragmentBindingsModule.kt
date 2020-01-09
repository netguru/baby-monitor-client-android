package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.FragmentScope
import co.netguru.baby.monitor.client.feature.client.configuration.AllDoneFragment
import co.netguru.baby.monitor.client.feature.client.configuration.ParentDeviceInfoFragment
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import co.netguru.baby.monitor.client.feature.client.home.livecamera.ClientLiveCameraFragment
import co.netguru.baby.monitor.client.feature.client.home.log.ClientActivityLogFragment
import co.netguru.baby.monitor.client.feature.communication.pairing.ConnectingDevicesFailedFragment
import co.netguru.baby.monitor.client.feature.communication.pairing.PairingFragment
import co.netguru.baby.monitor.client.feature.communication.pairing.ServiceDiscoveryFragment
import co.netguru.baby.monitor.client.feature.onboarding.VoiceRecordingsSettingsFragment
import co.netguru.baby.monitor.client.feature.onboarding.FeaturePresentationFragment
import co.netguru.baby.monitor.client.feature.onboarding.InfoAboutDevicesFragment
import co.netguru.baby.monitor.client.feature.onboarding.baby.*
import co.netguru.baby.monitor.client.feature.server.ChildMonitorFragment
import co.netguru.baby.monitor.client.feature.server.ChildMonitorFragmentModule
import co.netguru.baby.monitor.client.feature.settings.ClientSettingsFragment
import co.netguru.baby.monitor.client.feature.settings.ServerSettingsFragment
import co.netguru.baby.monitor.client.feature.splash.SplashFragment
import co.netguru.baby.monitor.client.feature.welcome.SpecifyDeviceFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class FragmentBindingsModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [ChildMonitorFragmentModule::class])
    internal abstract fun serverFragmentInjector(): ChildMonitorFragment

    @FragmentScope
    @ContributesAndroidInjector()
    internal abstract fun serverSettingsFragmentInjector(): ServerSettingsFragment

    @FragmentScope
    @ContributesAndroidInjector()
    internal abstract fun connectingDevicesFragmentInjector(): ServiceDiscoveryFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun clientLiveCameraFragmentInjector(): ClientLiveCameraFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun clientDashboardFragmentInjector(): ClientDashboardFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun splashFragmentInjector(): SplashFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun clientSettingsFragmentInjector(): ClientSettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun clientActivityLogFragmentInjector(): ClientActivityLogFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun featureDFragmentInjector(): VoiceRecordingsSettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun installAppFragment(): ParentDeviceInfoFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun pairingFragment(): PairingFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun infoAboutDevicesFragment(): InfoAboutDevicesFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun featurePresentationFragment(): FeaturePresentationFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun specifyDeviceFragment(): SpecifyDeviceFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun connectWifiFragment(): ConnectWifiFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun connectingDevicesFailedFragment(): ConnectingDevicesFailedFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun allDoneFragment(): AllDoneFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun permissionDeniedFragment(): PermissionDenied

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun permissionCameraFragment(): PermissionCameraFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun permissionMicrophoneFragment(): PermissionMicrophoneFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun setupInformationFragment(): SetupInformationFragment
}
