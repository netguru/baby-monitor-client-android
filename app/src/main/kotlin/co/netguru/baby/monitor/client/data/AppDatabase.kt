package co.netguru.baby.monitor.client.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.netguru.baby.monitor.client.data.client.ChildDataDao
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataDao
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.communication.ClientDataDao
import co.netguru.baby.monitor.client.data.communication.ClientEntity

@Database(
    entities = [
        LogDataEntity::class,
        ClientEntity::class,
        ChildDataEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDataDao(): LogDataDao
    abstract fun clientDao(): ClientDataDao
    abstract fun childDataDao(): ChildDataDao

    companion object {
        const val DATABASE_NAME = "baby-monitor-database"
    }
}
