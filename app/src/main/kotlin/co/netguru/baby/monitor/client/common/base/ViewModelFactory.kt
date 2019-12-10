package co.netguru.baby.monitor.client.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.client.home.livecamera.ClientLiveCameraFragmentViewModel
import co.netguru.baby.monitor.client.feature.server.ChildMonitorViewModel
import co.netguru.baby.monitor.client.feature.server.ServerViewModel
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import co.netguru.baby.monitor.client.feature.settings.SettingsViewModel
import co.netguru.baby.monitor.client.feature.splash.SplashViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Singleton
class ViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        viewModels[modelClass]?.get() as T
}

@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ClientHomeViewModel::class)
    abstract fun bindClientHomeActivityViewModel(clientHomeViewModel: ClientHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClientLiveCameraFragmentViewModel::class)
    abstract fun bindClientLiveCameraFragmentViewModel(clientHomeViewModel: ClientLiveCameraFragmentViewModel):
            ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfigurationViewModel::class)
    abstract fun bindConfigurationViewModel(configurationViewModel: ConfigurationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ServerViewModel::class)
    abstract fun bindServerViewModel(serverViewModel: ServerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashViewModel(lullabiesViewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChildMonitorViewModel::class)
    internal abstract fun bindChildMonitorViewModel(childMonitorViewModel: ChildMonitorViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
