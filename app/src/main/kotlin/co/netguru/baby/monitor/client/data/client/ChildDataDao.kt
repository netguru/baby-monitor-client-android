package co.netguru.baby.monitor.client.data.client

import android.arch.persistence.room.*
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ChildDataDao {

    @Query("SELECT * FROM CHILD_DATA")
    fun getAllChildren(): Flowable<List<ChildDataEntity>>

    @Query("SELECT * FROM CHILD_DATA WHERE address LIKE :address")
    fun getChildByAddress(address: String): Single<List<ChildDataEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChildData(data: ChildDataEntity)

    @Update
    fun updateChildData(data: ChildDataEntity)

    @Query("DELETE FROM LOG_DATA")
    fun deleteAll()
}
