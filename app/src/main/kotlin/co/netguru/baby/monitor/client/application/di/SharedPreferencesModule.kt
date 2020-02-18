package co.netguru.baby.monitor.client.application.di

import android.content.Context
import android.content.SharedPreferences
import co.netguru.baby.monitor.client.application.App
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class SharedPreferencesModule {

    @ConfigurationPreferencesQualifier
    @Singleton
    @Provides
    fun provideConfigurationSharedPreferences(app: App): SharedPreferences =
        app.getSharedPreferences(app.packageName + CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE)

    @FeedbackPreferencesQualifier
    @Singleton
    @Provides
    fun provideFeedbackSharedPreferences(app: App): SharedPreferences =
        app.getSharedPreferences(app.packageName + FEEDBACK_PREFERENCES, Context.MODE_PRIVATE)

    companion object {
        private const val CONFIGURATION_PREFERENCES = "configuration"
        private const val FEEDBACK_PREFERENCES = "feedback"
    }
}

@Qualifier
annotation class ConfigurationPreferencesQualifier
@Qualifier
annotation class FeedbackPreferencesQualifier
