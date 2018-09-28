package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.FragmentScope
import co.netguru.baby.monitor.client.feature.server.ServerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class FragmentBindingsModule {

    @FragmentScope
    @ContributesAndroidInjector()
    internal abstract fun serverFragmentInjector(): ServerFragment
}
