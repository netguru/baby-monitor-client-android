package co.netguru.baby.monitor.client.data.client

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CHILD_DATA")
data class ChildDataEntity(
    val address: String,
    var image: String? = null,
    var name: String? = null,
    var snoozeTimeStamp: Long? = null,
    // Right now we are supporting only one child
    @PrimaryKey val id: Int = 0
)
