package co.netguru.baby.monitor.client.data.client

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "CHILD_DATA")
data class ChildDataEntity(
    val address: String,
    var image: String? = null,
    var name: String? = null,
    //Right now we are supporting only one child
    @PrimaryKey val id: Int = 0
)
