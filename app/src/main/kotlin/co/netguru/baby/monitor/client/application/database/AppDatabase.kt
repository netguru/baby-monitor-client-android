package co.netguru.baby.monitor.client.application.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataDao
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataEntity

@Database(entities = [LogDataEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDataDao(): LogDataDao
}
