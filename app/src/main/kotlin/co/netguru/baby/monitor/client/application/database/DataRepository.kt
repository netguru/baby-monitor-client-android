package co.netguru.baby.monitor.client.application.database

import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.feature.client.home.log.database.LogDataEntity
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
}
