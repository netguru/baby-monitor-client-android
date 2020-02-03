package co.netguru.baby.monitor.client.application.di

import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.common.base.ViewModelModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        ViewModelModule::class,
        FragmentBindingsModule::class,
        ActivityBindingsModule::class,
        ServiceBindingsModule::class,
        SharedPreferencesModule::class,
        NotificationsModule::class,
        FirebaseModule::class,
        NetworkModule::class
    ]
)
internal interface ApplicationComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}
