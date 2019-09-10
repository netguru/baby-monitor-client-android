package co.netguru.baby.monitor.client.data.communication

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(
    tableName = "CLIENT_DATA",
    indices = [Index(value = ["firebase_key"], unique = true)]
)
data class ClientEntity(
        @ColumnInfo(name = "address") val address: String,
        @ColumnInfo(name = "firebase_key") var firebaseKey: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
