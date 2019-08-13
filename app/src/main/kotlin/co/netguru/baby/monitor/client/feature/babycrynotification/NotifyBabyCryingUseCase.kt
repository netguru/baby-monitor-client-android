package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import dagger.Reusable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@Reusable
class NotifyBabyCryingUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val httpClient: OkHttpClient
) {
    private fun postNotificationToFcm(to: String, title: String, text: String): Single<Response> =
        Single.fromCallable {
            httpClient.newCall(
                Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .header(
                        "Authorization",
                        "key=${BuildConfig.FIREBASE_CLOUD_MESSAGING_SERVER_KEY}"
                    )
                    .post(JSONObject().apply {
                        put("to", to)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("text", text)
                        })
                    }.toString().toRequestBody("application/json".toMediaType()))
                    .build()
            ).execute()
        }

    fun notifyBabyCrying(title: String, text: String) =
        dataRepository.getAllClientData()
            .firstOrError()
            .flattenAsObservable { it }
            .map(ClientEntity::firebaseKey)
            .flatMapSingle { firebaseToken ->
                postNotificationToFcm(firebaseToken, title, text)
            }
            .subscribeBy(
                onNext = { response ->
                    Timber.d("Response from Firebase: $response.")
                },
                onComplete = {
                    Timber.d("Sending push messages completed.")
                },
                onError = { error ->
                    Timber.w(error, "Sending push messages error.")
                }
            )
}
