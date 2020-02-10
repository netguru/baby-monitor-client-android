package co.netguru.baby.monitor.client.data

import androidx.lifecycle.LiveData
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.data.splash.AppStateHandler
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.UserProperty
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisOption
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val database: AppDatabase,
    private val appStateHandler: AppStateHandler,
    private val analyticsManager: AnalyticsManager
) {

    fun getAllLogData() = database.logDataDao().getAllData()

    fun insertLogToDatabase(data: LogDataEntity): Completable = Completable.fromAction {
        database.logDataDao().insertAll(data)
    }

    fun getClientData() = database.clientDao().getClientData()

    fun insertClientData(data: ClientEntity): Completable = Completable.fromAction {
        database.clientDao().insertClient(data)
    }

    fun getSavedState(): Single<AppState> = Single.fromCallable {
        appStateHandler.appState
    }

    fun saveConfiguration(state: AppState): Completable = Completable.fromAction {
        appStateHandler.appState = state
        analyticsManager.setUserProperty(UserProperty.AppStateProperty(state))
    }

    fun getChildData(): Maybe<ChildDataEntity> = database.childDataDao().getChildData()

    fun getChildLiveData(): LiveData<ChildDataEntity> = database.childDataDao().getChildLiveData()

    fun putChildData(entity: ChildDataEntity): Completable = Completable.fromAction {
        database.childDataDao().insertChildData(entity)
    }

    fun doesChildDataExists(address: String): Single<Boolean> {
        return Single.create { emitter ->
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

    fun updateVoiceAnalysisOption(voiceAnalysisOption: VoiceAnalysisOption) =
        Completable.fromAction {
            if (appStateHandler.appState == AppState.CLIENT) {
                database.childDataDao().updateVoiceAnalysisOption(voiceAnalysisOption)
            } else if (appStateHandler.appState == AppState.SERVER) {
                database.clientDao().updateVoiceAnalysisOption(voiceAnalysisOption)
                analyticsManager.setUserProperty(UserProperty.VoiceAnalysis(voiceAnalysisOption))
            }
        }

    fun updateNoiseSensitivity(sensitivity: Int) =
        Completable.fromAction {
            if (appStateHandler.appState == AppState.CLIENT) {
                database.childDataDao().updateNoiseSensitivity(sensitivity)
            } else if (appStateHandler.appState == AppState.SERVER) {
                database.clientDao().updateNoiseSensitivity(sensitivity)
                analyticsManager.setUserProperty(UserProperty.NoiseSensitivity(sensitivity))
            }
        }

    fun deleteAllData(): Completable = Completable.fromAction {
        database.childDataDao().deleteAll()
        database.clientDao().deleteAll()
        database.logDataDao().deleteAll()
        appStateHandler.appState = AppState.UNDEFINED
    }
}
