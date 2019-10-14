package co.netguru.baby.monitor.client.data.client

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
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

    @Query("DELETE FROM CHILD_DATA")
    fun deleteAll()
}
