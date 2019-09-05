package co.netguru.baby.monitor.client.feature.server

import co.netguru.baby.monitor.client.application.scope.FragmentScope
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
class ChildMonitorFragmentModule {
    @Provides
    @LowBatteryHandler
    @FragmentScope
    fun provideLowBatteryHandler(fragment: ChildMonitorFragment): () -> Unit =
        fragment::handleLowBattery

    @Qualifier
    annotation class LowBatteryHandler
}
