package co.netguru.baby.monitor.application

import co.netguru.baby.monitor.application.scope.ActivityScope
import co.netguru.baby.monitor.feature.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ActivityBindingsModule {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun splashActivityInjector(): SplashActivity
}
