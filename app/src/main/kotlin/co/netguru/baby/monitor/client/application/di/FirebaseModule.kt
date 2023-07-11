package co.netguru.baby.monitor.client.application.di

import android.content.Context
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object FirebaseModule {
    @Singleton
    @Provides
    fun firebaseRepository(preferencesWrapper: FirebaseSharedPreferencesWrapper, context: Context) =
        FirebaseRepository(preferencesWrapper, context)

    @Singleton
    @Provides
    fun firebaseInstanceManager() = FirebaseInstanceManager()

    @Provides
    @Singleton
    fun provideAnalyticsManager(context: Context) =
        AnalyticsManager(FirebaseAnalytics.getInstance(context))
}
