package co.netguru.baby.monitor.client.application

import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.feature.common.base.ViewModelModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@AppScope
@Component(
    modules = [
        AndroidInjectionModule::class,
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        ViewModelModule::class,
        FragmentBindingsModule::class,
        ActivityBindingsModule::class,
        ServiceBindingsModule::class,
        SharedPreferencesModule::class
    ]
)
internal interface ApplicationComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}
