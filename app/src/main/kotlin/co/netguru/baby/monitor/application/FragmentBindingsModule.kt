package co.netguru.baby.monitor.application

import co.netguru.baby.monitor.application.scope.FragmentScope
import co.netguru.baby.monitor.feature.client.configuration.ConfigurationFragment
import co.netguru.baby.monitor.feature.client.livecamera.ClientLiveCameraFragment
import co.netguru.baby.monitor.feature.server.ServerFragment
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
    internal abstract fun ClientLiveCameraFragmentInjector(): ClientLiveCameraFragment
}
