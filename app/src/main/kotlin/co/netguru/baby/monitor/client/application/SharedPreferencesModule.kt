package co.netguru.baby.monitor.client.application

import android.content.Context
import android.content.SharedPreferences
import co.netguru.baby.monitor.client.application.scope.AppScope
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Module
class SharedPreferencesModule {

    @ConfigurationPreferencesQualifier
    @AppScope
    @Provides
    fun provideConfigurationSharedPreferences(app: App): SharedPreferences =
        app.getSharedPreferences(app.packageName + CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)

    companion object {
        private const val CONFIGURATION_PREFERENCES = "configuration"
    }
}

@Qualifier
annotation class ConfigurationPreferencesQualifier
