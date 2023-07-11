package co.netguru.baby.monitor.client.application.di

import co.netguru.baby.monitor.client.feature.batterylevel.LowBatteryPublishSubject
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LowBatteryModule {
    @Provides
    @Singleton
    fun provideLowBatteryHandler() : LowBatteryPublishSubject = LowBatteryPublishSubject()
}
