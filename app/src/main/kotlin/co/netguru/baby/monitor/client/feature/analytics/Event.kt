package co.netguru.baby.monitor.client.feature.analytics

import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import java.util.*

sealed class Event(open val eventType: EventType) {
    data class Simple(override val eventType: EventType) : Event(eventType)
    sealed class ParamEvent(
        override val eventType: EventType,
        val param: Pair<EventParam, Any>
    ) : Event(eventType) {
        data class NotificationSent(val value: NotificationType) : ParamEvent(
            EventType.NOTIFICATION_SENT,
            EventParam.TYPE to value.name.toLowerCase(Locale.getDefault())
        )

        data class NightMode(val value: Boolean) : ParamEvent(
            EventType.NIGHT_MODE,
            EventParam.IS_ENABLED to value
        )
    }
}

enum class EventType(val eventName: String) {
    NOTIFICATION_SENT("notification_sent"),
    NOTIFICATION_SNOOZE("notification_snooze"),
    NOTIFICATION_OPEN_CAMERA("notification_open_camera"),
    RESET_APP("reset_app"),
    NIGHT_MODE("night_mode"),
    VIDEO_STREAM_CONNECTED("video_stream_connected"),
    VIDEO_STREAM_ERROR("video_stream_error")
}

enum class EventParam(val paramName: String) {
    TYPE("type"),
    IS_ENABLED("is_enabled")
}
