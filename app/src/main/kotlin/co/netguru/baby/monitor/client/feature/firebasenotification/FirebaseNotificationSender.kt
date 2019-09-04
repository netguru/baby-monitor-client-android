package co.netguru.baby.monitor.client.feature.firebasenotification

import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class FirebaseNotificationSender @Inject constructor(
    private val dataRepository: DataRepository,
    private val httpClient: OkHttpClient
) {
    fun broadcastNotificationToFcm(title: String, text: String): Completable =
        dataRepository.getAllClientData()
            .doOnSubscribe { Timber.i("Subscribing to all client data.") }
            .doOnNext { Timber.i("Next client data: $it.") }
            .doOnTerminate { Timber.i("Terminated all client data.") }
            .firstOrError()
            .map { clients -> clients.map(ClientEntity::firebaseKey) }
            .flatMapCompletable { firebaseTokens ->
                if (firebaseTokens.isNotEmpty())
                    postNotificationToFcm(firebaseTokens, title, text)
                        .doOnSuccess { response ->
                            Timber.d("Posting notification succeeded: $response.")
                            Timber.d(response.body()?.string().toString())
                        }
                        .ignoreElement()
                else
                    Completable.complete()
            }

    private fun postNotificationToFcm(
        to: List<String>,
        title: String,
        text: String
    ): Single<Response> =
        Single.fromCallable {
            httpClient.newCall(
                Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .header(
                        "Authorization",
                        "key=${BuildConfig.FIREBASE_CLOUD_MESSAGING_SERVER_KEY}"
                    )
                    .post(JSONObject().apply {
                        put("registration_ids", JSONArray(to))
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("text", text)
                        })
                    }.toString().let { body ->
                        RequestBody.create(MediaType.get("application/json"), body)
                    })
                    .build()
            ).execute()
        }

}