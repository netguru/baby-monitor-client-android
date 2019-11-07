package co.netguru.baby.monitor.client.data.communication

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
