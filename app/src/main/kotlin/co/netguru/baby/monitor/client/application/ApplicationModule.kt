package co.netguru.baby.monitor.client.application

import android.content.Context
import android.net.nsd.NsdManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.common.SchedulersProvider
import co.netguru.baby.monitor.client.data.AppDatabase
import co.netguru.baby.monitor.client.feature.babycrynotification.NotifyBabyCryingUseCase
import co.netguru.baby.monitor.client.feature.communication.nsd.DeviceNameProvider
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Singleton
    @Provides
    fun rxJavaErrorHandler(): RxJavaErrorHandler = RxJavaErrorHandlerImpl()

    @Reusable
    @Provides
    fun nsdManager(app: App): NsdManager = app.getSystemService(Context.NSD_SERVICE) as NsdManager

    @Provides
    fun context(app: App): Context = app.applicationContext

    @Provides
    fun nsdServiceManager(nsdManager: NsdManager, deviceNameProvider: DeviceNameProvider) =
        NsdServiceManager(nsdManager, deviceNameProvider)

    @Provides
    fun schedulersProvider(): ISchedulersProvider = SchedulersProvider()

    @Provides
    fun notificationHandler(context: Context) = NotificationHandler(context)

    @Singleton
    @Provides
    fun firebaseRepository(preferencesWrapper: FirebaseSharedPreferencesWrapper, context: Context) =
        FirebaseRepository(preferencesWrapper, context)

    @Singleton
    @Provides
    fun firebaseInstanceManager() = FirebaseInstanceManager(FirebaseInstanceId.getInstance())

    @Singleton
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

    @Provides
    @Reusable
    fun provideNotifyBabyCryingUseCase(
        notificationSender: FirebaseNotificationSender,
        context: Context
    ) =
        NotifyBabyCryingUseCase(
            notificationSender,
            context.resources.getString(R.string.notification_baby_is_crying_title),
            context.resources.getString(R.string.notification_baby_is_crying_content)
        )

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
