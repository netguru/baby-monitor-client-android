package co.netguru.baby.monitor.client.application.database

import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataEntity
import co.netguru.baby.monitor.client.feature.communication.webrtc.database.ClientEntity
import io.reactivex.Completable
import javax.inject.Inject

@AppScope
class DataRepository @Inject constructor(
        private val database: AppDatabase
) {

    fun getAllLogData() = database.logDataDao().getAllData()

    fun insertLogsToDatabase(vararg data: LogDataEntity) = Completable.fromAction {
        for (log in data) {
            database.logDataDao().insertAll(log)
        }
    }

    fun getAllClientData() = database.clientDao().getAllData()

    fun insertClientData(data: ClientEntity) = Completable.fromAction {
        database.clientDao().insertClient(data)
    }

    fun insertClientData(data: List<ClientEntity>) = Completable.fromAction {
        database.clientDao().insertAllClients(data)
    }
}
