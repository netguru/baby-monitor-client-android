package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.FragmentScope
import co.netguru.baby.monitor.client.feature.client.configuration.ConfigurationFragment
import co.netguru.baby.monitor.client.feature.client.home.dashboard.ClientDashboardFragment
import co.netguru.baby.monitor.client.feature.client.home.livecamera.ClientLiveCameraFragment
import co.netguru.baby.monitor.client.feature.client.home.talk.ClientTalkFragment
import co.netguru.baby.monitor.client.feature.server.ServerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class FragmentBindingsModule {

    @FragmentScope
    @ContributesAndroidInjector()
    internal abstract fun serverFragmentInjector(): ServerFragment

    @FragmentScope
    @ContributesAndroidInjector()
    internal abstract fun configurationFragmentInjector(): ConfigurationFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun clientLiveCameraFragmentInjector(): ClientLiveCameraFragment

    @ContributesAndroidInjector
    internal abstract fun clientDashboardFragmentInjector(): ClientDashboardFragment

    @ContributesAndroidInjector
    internal abstract fun clientTalkFragmentInjector(): ClientTalkFragment
}
