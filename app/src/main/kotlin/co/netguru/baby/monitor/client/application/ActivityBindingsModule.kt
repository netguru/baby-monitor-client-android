package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.ActivityScope
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeActivity
import co.netguru.baby.monitor.client.feature.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivityBindingsModule {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun splashActivityInjector(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun clientHomeActivityInjector(): ClientHomeActivity
}
