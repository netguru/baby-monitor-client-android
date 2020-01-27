package co.netguru.baby.monitor.client.feature.firebasenotification

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("to")
    val firebaseToken: String,
    @SerializedName("notification")
    val notification: Notification? = null,
    @SerializedName("data")
    val data: Data? = null
)

data class Notification(
    @SerializedName("title")
    val title: String,
    @SerializedName("text")
    val text: String
)

data class Data(
    @SerializedName("title")
    val title: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("notification_type")
    val type: String
)
