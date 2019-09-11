package co.netguru.baby.monitor.client.data

import co.netguru.baby.monitor.client.application.scope.AppScope
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.data.splash.AppStateHandler
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

@AppScope
class DataRepository @Inject constructor(
        private val database: AppDatabase,
        private val appStateHandler: AppStateHandler
) {

    fun getAllLogData() = database.logDataDao().getAllData()

    fun insertLogToDatabase(data: LogDataEntity) = Completable.fromAction {
        database.logDataDao().insertAll(data)
    }

    fun getAllClientData() = database.clientDao().getAllData()

    fun insertClientData(data: ClientEntity) = Completable.fromAction {
        database.clientDao().insertClient(data)
    }

    fun insertClientData(data: List<ClientEntity>) = Completable.fromAction {
        database.clientDao().insertAllClients(data)
    }

    fun getSavedState() = Single.fromCallable {
        appStateHandler.appState
    }

    fun saveConfiguration(state: AppState) = Completable.fromAction {
        appStateHandler.appState = state
    }

    fun getChildData() = database.childDataDao().getAllChildren()

    fun listChildren(): Flowable<List<ChildDataEntity>> = database.childDataDao().listChildren()

    fun getFirstChild() = database.childDataDao().getFirstChild()

    fun putChildData(entity: ChildDataEntity): Completable = Completable.fromAction {
        database.beginTransaction()
        try {
            database.childDataDao().apply {
                deleteAll()
                insertChildData(entity)
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    fun deleteAllData() = Completable.fromAction {
        database.childDataDao().deleteAll()
        database.clientDao().deleteAll()
        database.logDataDao().deleteAll()
        appStateHandler.appState = AppState.UNDEFINED
    }
}
