package co.netguru.baby.monitor.client.application

import android.arch.persistence.room.Room
import android.content.Context
import android.net.nsd.NsdManager
import co.netguru.baby.monitor.client.data.AppDatabase
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
class ApplicationModule {

    @AppScope
    @Provides
    fun rxJavaErrorHandler(): RxJavaErrorHandler = RxJavaErrorHandlerImpl()

    @Reusable
    @Provides
    fun nsdManager(app: App): NsdManager = app.getSystemService(Context.NSD_SERVICE) as NsdManager

    @Provides
    fun context(app: App): Context = app.applicationContext

    @Provides
    fun nsdServiceManager(nsdManager: NsdManager) = NsdServiceManager(nsdManager)

    @Provides
    fun lullabyPlayer(app: App) = LullabyPlayer(app)

    @Provides
    fun notificationHandler(context: Context) = NotificationHandler(context)

    @AppScope
    @Provides
    fun firebaseRepository(preferencesWrapper: FirebaseSharedPreferencesWrapper, context: Context) = FirebaseRepository(preferencesWrapper, context)

    @AppScope
    @Provides
    fun applicationDatabse(context: Context) =
            Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "baby-monitor-database"
            ).build()
}
