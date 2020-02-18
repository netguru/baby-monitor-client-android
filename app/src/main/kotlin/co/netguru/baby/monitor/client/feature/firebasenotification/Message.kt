package co.netguru.baby.monitor.client.feature.firebasenotification

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("to")
    val firebaseToken: String,
    @SerializedName("notification")
    val notification: Notification? = null,
    @SerializedName("data")
    val notificationData: NotificationData? = null
)

data class Notification(
    @SerializedName("title")
    val title: String,
    @SerializedName("text")
    val text: String
)

data class NotificationData(
    @SerializedName(FirebaseNotificationSender.NOTIFICATION_TITLE)
    val title: String,
    @SerializedName(FirebaseNotificationSender.NOTIFICATION_TEXT)
    val text: String,
    @SerializedName(FirebaseNotificationSender.NOTIFICATION_TYPE)
    val type: NotificationType,
    @SerializedName(FirebaseNotificationSender.FEEDBACK_RECORDING_FILE)
    val feedbackRecordingFile: String = ""
)
