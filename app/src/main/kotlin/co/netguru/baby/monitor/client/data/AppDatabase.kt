package co.netguru.baby.monitor.client.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
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
        version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDataDao(): LogDataDao
    abstract fun clientDao(): ClientDataDao
    abstract fun childDataDao(): ChildDataDao
}
