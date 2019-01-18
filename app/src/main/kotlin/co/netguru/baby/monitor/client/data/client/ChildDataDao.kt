package co.netguru.baby.monitor.client.data.client

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Flowable

@Dao
interface ChildDataDao {

    @Query("SELECT * FROM CHILD_DATA")
    fun getAllChildren(): Flowable<List<ChildDataEntity>>

    @Insert
    fun insertChildData(data: ChildDataEntity)

    @Update
    fun updateChildData(data: ChildDataEntity)

    @Query("DELETE FROM LOG_DATA")
    fun deleteAll()
}
