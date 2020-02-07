package co.netguru.baby.monitor.client.application.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.application.RxJavaErrorHandler
import co.netguru.baby.monitor.client.application.RxJavaErrorHandlerImpl
import co.netguru.baby.monitor.client.common.ISchedulersProvider
import co.netguru.baby.monitor.client.common.SchedulersProvider
import co.netguru.baby.monitor.client.data.AppDatabase
import co.netguru.baby.monitor.client.data.AppDatabase.Companion.DATABASE_NAME
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Suppress("MagicNumber")
object ApplicationModule {

    @Singleton
    @Provides
    fun rxJavaErrorHandler(): RxJavaErrorHandler =
        RxJavaErrorHandlerImpl()

    @Provides
    fun context(app: App): Context = app.applicationContext

    @Provides
    fun schedulersProvider(): ISchedulersProvider = SchedulersProvider()

    @Singleton
    @Provides
    fun machineLearning(context: Context): MachineLearning = MachineLearning(context)

    @Singleton
    @Provides
    fun applicationDatabase(context: Context) =
        try {
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                .build()
        } catch (e: IllegalStateException) {
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME

            )
                .fallbackToDestructiveMigration()
                .build()
        }

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
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE CHILD_DATA ADD COLUMN voiceAnalysisOption  TEXT NOT NULL DEFAULT ${VoiceAnalysisOption.MACHINE_LEARNING.name}")
            database.execSQL("ALTER TABLE CLIENT_DATA ADD COLUMN voiceAnalysisOption TEXT NOT NULL DEFAULT ${VoiceAnalysisOption.MACHINE_LEARNING.name}")
        }
    }
}
