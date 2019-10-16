package co.netguru.baby.monitor.client.application

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.net.nsd.NsdManager
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.AppDatabase
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient

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
    fun firebaseRepository(preferencesWrapper: FirebaseSharedPreferencesWrapper, context: Context) =
        FirebaseRepository(preferencesWrapper, context)

    @AppScope
    @Provides
    fun applicationDatabse(context: Context) =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "baby-monitor-database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    @Reusable
    fun provideGson() =
        Gson()

    @Provides
    @Reusable
    fun provideOkHttp() =
        OkHttpClient.Builder()
            .build()

    @Suppress("MagicNumber")
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DELETE FROM CLIENT_DATA")
                database.execSQL("CREATE UNIQUE INDEX index_client_data_firebase_key ON CLIENT_DATA(firebase_key)")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE CHILD_DATA ADD COLUMN snoozeTimeStamp INTEGER")
            }
        }
    }
}
