package co.netguru.baby.monitor.client.feature.firebasenotification

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("to")
    val firebaseToken: String,
    val notification: Notification? = null,
    val data: Data? = null
)

data class Notification(
    val title: String,
    val text: String
)

data class Data(
    val title: String,
    val text: String,
    @SerializedName("notification_type")
    val type: NotificationType?
)
