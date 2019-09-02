package co.netguru.baby.monitor.client.data.communication

import android.arch.persistence.room.*
import io.reactivex.Flowable

@Dao
interface ClientDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClient(data: ClientEntity)

    @Insert
    fun insertAllClients(clientEntity: List<ClientEntity>)

    @Query("SELECT * FROM CLIENT_DATA")
    fun getAllData(): Flowable<List<ClientEntity>>

    @Query("DELETE FROM LOG_DATA")
    fun deleteAll()
}
