package co.netguru.baby.monitor.client.data.client

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption

@Entity(tableName = "CHILD_DATA")
data class ChildDataEntity(
    val address: String,
    var image: String? = null,
    val name: String? = null,
    val snoozeTimeStamp: Long? = null,
    val voiceAnalysisOption: VoiceAnalysisOption = VoiceAnalysisOption.MachineLearning,
    // Right now we are supporting only one child
    @PrimaryKey val id: Int = 0
)
