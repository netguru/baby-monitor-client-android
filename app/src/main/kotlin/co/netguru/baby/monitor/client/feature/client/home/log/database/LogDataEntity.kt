package co.netguru.baby.monitor.client.feature.client.home.log.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogData
import org.threeten.bp.LocalDateTime

@Entity(tableName = "LOG_DATA")
data class LogDataEntity(
        @ColumnInfo(name = "action") val action: String,
        @ColumnInfo(name = "time_stamp") val timeStamp: String,
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
