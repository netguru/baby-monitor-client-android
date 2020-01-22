package co.netguru.baby.monitor.client.feature.firebasenotification

import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.Event
import co.netguru.baby.monitor.client.feature.debug.DebugModule
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FirebaseNotificationSender @Inject constructor(
    private val dataRepository: DataRepository,
    private val httpClient: OkHttpClient,
    private val debugModule: DebugModule,
    private val analyticsManager: AnalyticsManager,
    private val gson: Gson
) {
    fun broadcastNotificationToFcm(
        title: String,
        text: String,
        notificationType: NotificationType
    ): Completable =
        dataRepository.getClientData()
            .doOnSuccess { Timber.i("Client data: $it.") }
            .map { client -> client.firebaseKey }
            .flatMapCompletable { firebaseToken ->
                analyticsManager.logEvent(Event.ParamEvent.NotificationSent(notificationType))
                postNotificationToFcm(firebaseToken, title, text, notificationType)
                    .doOnSuccess { response ->
                        debugModule.sendNotificationEvent(
                            "Notification posted: Title: $title" +
                                    " text: $text. Response: ${response.body?.string()}."
                        )
                        Timber.d("Notification posted: $response.")
                    }
                    .ignoreElement()
            }

    private fun postNotificationToFcm(
        firebaseToken: String,
        title: String,
        text: String,
        notificationType: NotificationType
    ): Single<Response> {
        return checkIdCall(firebaseToken)
            .flatMap { isAndroid ->
                return@flatMap notificationCall(
                    firebaseToken,
                    title,
                    text,
                    notificationType,
                    isAndroid
                )
            }
    }

    private fun checkIdCall(firebaseToken: String): Single<Boolean> {
        return Single.fromCallable {
            val response = httpClient.newCall(
                Request.Builder()
                    .url("$ID_CHECK_URL$firebaseToken")
                    .header(
                        AUTHORIZATION_HEADER,
                        "key=${BuildConfig.FIREBASE_CLOUD_MESSAGING_SERVER_KEY}"
                    )
                    .build()
            ).execute()
            val platform = response.body?.let { JSONObject(it.string()) }
                ?.getString(PLATFORM_KEY)
            return@fromCallable platform?.toLowerCase(Locale.getDefault()) == ANDROID_PLATFORM
        }
            .onErrorReturnItem(false)
    }

    private fun notificationCall(
        firebaseToken: String,
        title: String,
        text: String,
        notificationType: NotificationType,
        isAndroid: Boolean
    ): Single<Response> {
        return Single.fromCallable {
            httpClient.newCall(
                Request.Builder()
                    .url(FCM_URL)
                    .header(
                        AUTHORIZATION_HEADER,
                        "key=${BuildConfig.FIREBASE_CLOUD_MESSAGING_SERVER_KEY}"
                    )
                    .post(
                        gson.toJson(
                            if (isAndroid) {
                                Message(firebaseToken, data = Data(title, text, notificationType))
                            } else {
                                Message(firebaseToken, notification = Notification(title, text))
                            }
                        )
                            .toString()
                            .toRequestBody("application/json".toMediaType())
                    )
                    .build()
            ).execute()
        }
    }

    companion object {
        private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
        private const val ID_CHECK_URL = "https://iid.googleapis.com/iid/info/"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val ANDROID_PLATFORM = "android"
        private const val PLATFORM_KEY = "platform"
        const val NOTIFICATION_TEXT = "text"
        const val NOTIFICATION_TITLE = "title"
        const val NOTIFICATION_TYPE = "notification_type"
    }
}
