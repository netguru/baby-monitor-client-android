package co.netguru.baby.monitor.client.feature.common

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.feature.client.configuration.ConfigurationViewModel
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.client.home.lullabies.LullabiesViewModel
import co.netguru.baby.monitor.client.feature.server.ServerViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@AppScope
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
    @ViewModelKey(ConfigurationViewModel::class)
    abstract fun bindConfigurationViewModel(configurationViewModel: ConfigurationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ServerViewModel::class)
    abstract fun bindServerViewModel(serverViewModel: ServerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LullabiesViewModel::class)
    abstract fun bindLullabiesViewModel(lullabiesViewModel: LullabiesViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
