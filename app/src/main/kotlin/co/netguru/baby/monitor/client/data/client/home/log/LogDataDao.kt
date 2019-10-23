package co.netguru.baby.monitor.client.data.client.home.log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.*

@Dao
interface LogDataDao {

    @Query("SELECT * FROM LOG_DATA")
    fun getAllData(): Flowable<List<LogDataEntity>>

    @Insert
    fun insertAll(vararg logs: LogDataEntity)

    @Query("DELETE FROM LOG_DATA")
    fun deleteAll()
}
