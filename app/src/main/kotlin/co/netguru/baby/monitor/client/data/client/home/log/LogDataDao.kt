package co.netguru.baby.monitor.client.data.client.home.log

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
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
