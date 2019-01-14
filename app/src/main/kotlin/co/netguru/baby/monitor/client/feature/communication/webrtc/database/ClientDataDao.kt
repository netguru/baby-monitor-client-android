package co.netguru.baby.monitor.client.feature.communication.webrtc.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface ClientDataDao {

    @Insert
    fun insertClient(data: ClientEntity)

    @Insert
    fun insertAllClients(clientEntity: List<ClientEntity>)

    @Query("SELECT * FROM CLIENT_DATA")
    fun getAllData(): Flowable<List<ClientEntity>>

    @Query("DELETE FROM LOG_DATA")
    fun deleteAll()
}
