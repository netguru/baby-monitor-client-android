package co.netguru.baby.monitor.client.data.client.home.log

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity(tableName = "LOG_DATA")
data class LogDataEntity(
    @ColumnInfo(name = "action") val action: String,
    @ColumnInfo(name = "time_stamp") val timeStamp: String = LocalDateTime.now().toString(),
    @ColumnInfo(name = "image") val address: String? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    fun toLogData(image: String?): LogData.Data =
            LogData.Data(
                    action = this.action,
                    timeStamp = LocalDateTime.parse(this.timeStamp),
                    image = image ?: ""
            )
}
