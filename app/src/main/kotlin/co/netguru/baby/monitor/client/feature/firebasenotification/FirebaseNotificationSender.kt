package co.netguru.baby.monitor.client.feature.firebasenotification

import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class FirebaseNotificationSender @Inject constructor(
    private val dataRepository: DataRepository,
    private val httpClient: OkHttpClient
) {
    fun broadcastNotificationToFcm(
        title: String,
        text: String,
        notificationType: NotificationType
    ): Completable =
        dataRepository.getAllClientData()
            .doOnSubscribe { Timber.i("Subscribing to all client data.") }
            .doOnNext { Timber.i("Next client data: $it.") }
            .doOnTerminate { Timber.i("Terminated all client data.") }
            .firstOrError()
            .map { clients -> clients.map(ClientEntity::firebaseKey) }
            .flatMapCompletable { firebaseTokens ->
                if (firebaseTokens.isNotEmpty()) {
                    postNotificationToFcm(firebaseTokens, title, text, notificationType)
                        .doOnSuccess { response ->
                            Timber.d("Posting notification succeeded: $response.")
                            Timber.d(response.body?.string().toString())
                        }
                        .ignoreElement()
                } else {
                    Completable.complete()
                }
            }

    private fun postNotificationToFcm(
        to: List<String>,
        title: String,
        text: String,
        notificationType: NotificationType
    ): Single<Response> =
        Single.fromCallable {
            httpClient.newCall(
                Request.Builder()
                    .url(FCM_URL)
                    .header(
                        AUTHORIZATION_HEADER,
                        "key=${BuildConfig.FIREBASE_CLOUD_MESSAGING_SERVER_KEY}"
                    )
                    .post(
                        JSONObject().apply {
                            put("registration_ids", JSONArray(to))
                            put(NOTIFICATION_DATA, JSONObject().apply {
                                put(NOTIFICATION_TITLE, title)
                                put(NOTIFICATION_TEXT, text)
                                put(NOTIFICATION_TYPE, notificationType.name)
                            })
                        }.toString()
                            .toRequestBody("application/json".toMediaType())
                    )
                    .build()
            ).execute()
        }

    companion object {
        private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val NOTIFICATION_DATA = "data"
        const val NOTIFICATION_TEXT = "text"
        const val NOTIFICATION_TITLE = "title"
        const val NOTIFICATION_TYPE = "notification_type"
    }
}
