package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.FragmentScope
import co.netguru.baby.monitor.client.feature.client.configuration.ConfigurationFragment
import co.netguru.baby.monitor.client.feature.client.configuration.SecondAppInfo
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import co.netguru.baby.monitor.client.feature.client.home.livecamera.ClientLiveCameraFragment
import co.netguru.baby.monitor.client.feature.client.home.log.ClientActivityLogFragment
import co.netguru.baby.monitor.client.feature.client.home.lullabies.ClientLullabiesFragment
import co.netguru.baby.monitor.client.feature.server.ChildMonitorFragment
import co.netguru.baby.monitor.client.feature.settings.ServerSettingsFragment
import co.netguru.baby.monitor.client.feature.splash.SplashFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class FragmentBindingsModule {

    @FragmentScope
    @ContributesAndroidInjector()
    internal abstract fun serverFragmentInjector(): ChildMonitorFragment

    @FragmentScope
    @ContributesAndroidInjector()
    internal abstract fun configurationFragmentInjector(): ConfigurationFragment

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
    internal abstract fun clientLullabiesFragmentInjector(): ClientLullabiesFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun clientSettingsFragmentInjector(): ServerSettingsFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun clientActivityLogFragmentInjector(): ClientActivityLogFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun installAppFragment(): SecondAppInfo
}
