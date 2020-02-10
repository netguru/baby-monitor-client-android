package co.netguru.baby.monitor.client.data.communication

import androidx.room.*
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import io.reactivex.Maybe

@Dao
interface ClientDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClient(data: ClientEntity)

    @Query("SELECT * FROM CLIENT_DATA LIMIT 1")
    fun getClientData(): Maybe<ClientEntity>

    @Query("DELETE FROM CLIENT_DATA")
    fun deleteAll()

    @Query("UPDATE CLIENT_DATA SET voiceAnalysisOption = :voiceAnalysisOption WHERE id = 0")
    fun updateVoiceAnalysisOption(voiceAnalysisOption: VoiceAnalysisOption)

    @Query("UPDATE CLIENT_DATA SET noiseSensitivity = :sensitivity WHERE id = 0")
    fun updateNoiseSensitivity(sensitivity: Int)
}
