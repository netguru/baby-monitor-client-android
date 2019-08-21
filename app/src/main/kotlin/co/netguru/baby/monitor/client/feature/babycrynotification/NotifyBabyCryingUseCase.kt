package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import dagger.Reusable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class NotifyBabyCryingUseCase @Inject constructor(
    private val dataRepository: DataRepository,
    private val httpClient: OkHttpClient
) {
    private val babyCryingEvents: PublishSubject<BabyCrying> = PublishSubject.create()

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
                        put("data", JSONObject().apply {
                            put("title", title)
                            put("text", text)
                        })
                    }.toString().let { body ->
                        RequestBody.create(MediaType.get("application/json"), body)
                    })
                    .build()
            ).execute()
        }

    private fun fetchClientsAndPostNotification(title: String, text: String) =
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

    fun subscribe(title: String, text: String): Disposable =
        babyCryingEvents
            .throttleFirst(1, TimeUnit.MINUTES)
            .subscribe {
                fetchClientsAndPostNotification(title = title, text = text)
            }

    fun notifyBabyCrying() =
        babyCryingEvents.onNext(BabyCrying)

    private object BabyCrying
}
