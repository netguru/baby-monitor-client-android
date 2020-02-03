package co.netguru.baby.monitor.client.data

import androidx.room.TypeConverter
import co.netguru.baby.monitor.client.feature.machinelearning.VoiceAnalysisOption

class Converters {
    @TypeConverter
    fun fromName(name: String) = VoiceAnalysisOption.valueOf(name)

    @TypeConverter
    fun optionToString(voiceAnalysisOption: VoiceAnalysisOption) = voiceAnalysisOption.name
}
