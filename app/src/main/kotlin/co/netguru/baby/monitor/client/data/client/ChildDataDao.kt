package co.netguru.baby.monitor.client.data.client

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import io.reactivex.Maybe

@Dao
interface ChildDataDao {

    @Query("SELECT * FROM CHILD_DATA LIMIT 1")
    fun getChildData(): Maybe<ChildDataEntity>

    @Query("SELECT * FROM CHILD_DATA LIMIT 1")
    fun getChildLiveData(): LiveData<ChildDataEntity>

    @Query("SELECT COUNT(id) FROM CHILD_DATA WHERE address LIKE :address")
    fun getCount(address: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChildData(data: ChildDataEntity)

    @Query("UPDATE CHILD_DATA SET name = :name WHERE id = 0")
    fun updateChildName(name: String): Int

    @Query("UPDATE CHILD_DATA SET snoozeTimeStamp = :notificationSnoozeTimeStamp WHERE id = 0")
    fun updateNotificationSnoozeTimeStamp(notificationSnoozeTimeStamp: Long): Int

    @Query("UPDATE CHILD_DATA SET voiceAnalysisOption = :voiceAnalysisOption WHERE id = 0")
    fun updateVoiceAnalysisOption(voiceAnalysisOption: VoiceAnalysisOption)

    @Query("UPDATE CHILD_DATA SET noiseLevel = :noiseLevel WHERE id = 0")
    fun updateNoiseLevel(noiseLevel: Int)

    @Query("DELETE FROM CHILD_DATA")
    fun deleteAll()
}
