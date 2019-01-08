package co.netguru.baby.monitor.client.application.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataDao
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataEntity
import co.netguru.baby.monitor.client.feature.communication.webrtc.database.ClientDataDao
import co.netguru.baby.monitor.client.feature.communication.webrtc.database.ClientEntity

@Database(
        entities = [
            LogDataEntity::class,
            ClientEntity::class
        ],
        version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDataDao(): LogDataDao
    abstract fun clientDao(): ClientDataDao
}
