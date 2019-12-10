package co.netguru.baby.monitor.client.data

import androidx.lifecycle.LiveData
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.data.splash.AppStateHandler
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val database: AppDatabase,
    private val appStateHandler: AppStateHandler
) {

    fun getAllLogData() = database.logDataDao().getAllData()

    fun insertLogToDatabase(data: LogDataEntity): Completable = Completable.fromAction {
        database.logDataDao().insertAll(data)
    }

    fun getAllClientData() = database.clientDao().getAllData()

    fun insertClientData(data: ClientEntity): Completable = Completable.fromAction {
        database.clientDao().insertClient(data)
    }

    fun getSavedState(): Single<AppState> = Single.fromCallable {
        appStateHandler.appState
    }

    fun saveConfiguration(state: AppState): Completable = Completable.fromAction {
        appStateHandler.appState = state
    }

    fun getChildData(): Maybe<ChildDataEntity> = database.childDataDao().getChildData()

    fun getChildLiveData(): LiveData<ChildDataEntity> = database.childDataDao().getChildLiveData()

    fun putChildData(entity: ChildDataEntity): Completable = Completable.fromAction {
        database.childDataDao().insertChildData(entity)
    }

    fun doesChildDataExists(address: String): Single<Boolean> {
        return Single.create {
            emitter ->
            val count = database.childDataDao().getCount(address)
            emitter.onSuccess(count > 0)
        }
    }

    fun updateChildName(name: String): Completable = Completable.fromAction {
        database.childDataDao().updateChildName(name)
    }

    fun updateChildSnoozeTimestamp(timestamp: Long): Completable = Completable.fromAction {
        database.childDataDao().updateNotificationSnoozeTimeStamp(timestamp)
    }

    fun deleteAllData(): Completable = Completable.fromAction {
        database.childDataDao().deleteAll()
        database.clientDao().deleteAll()
        database.logDataDao().deleteAll()
        appStateHandler.appState = AppState.UNDEFINED
    }
}
