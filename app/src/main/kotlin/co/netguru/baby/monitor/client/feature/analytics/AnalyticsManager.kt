package co.netguru.baby.monitor.client.feature.analytics

import android.app.Activity
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class AnalyticsManager(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun setCurrentScreen(activity: Activity, screenName: String) {
        firebaseAnalytics.setCurrentScreen(activity, screenName, null)
        Timber.d("$SCREEN $screenName")
    }

    fun logEvent(eventName: String) {
        firebaseAnalytics.logEvent(eventName, null)
        Timber.d("$EVENT $eventName")
    }

    fun logEventWithParam(eventName: String, pair: Pair<String, Any>) {
        firebaseAnalytics.logEvent(eventName, bundleOf(pair))
        Timber.d("$EVENT $eventName")
    }

    companion object {
        private const val EVENT = "event"
        private const val SCREEN = "screen"

        // Log events
        const val NOTIFICATION_SENT_EVENT = "notification_sent"
        const val TYPE_PARAM = "type"

        const val NOTIFICATION_SNOOZE_EVENT = "notification_snooze"
        const val NOTIFICATION_OPEN_CAMERA_EVENT = "notification_open_camera"

        const val RESET_APP_EVENT = "reset_app"

        const val NIGHT_MODE_EVENT = "night_mode"
        const val ENABLED_PARAM = "is_enabled"

        const val VIDEO_STREAM_CONNECTED = "video_stream_connected"
        const val VIDEO_STREAM_ERROR = "video_stream_error"

        // Screen names
        // General
        const val SPLASH = "Splash"
        const val ONBOARDING = "Onboarding"
        const val INFO_ABOUT_DEVICES = "InfoAboutDevices"
        const val SPECIFY_DEVICE = "SpecifyDevice"
        // Child Device
        const val VOICE_RECORDINGS_SETTING = "VoiceRecordingsSetting"
        const val CONNECT_WIFI = "ConnectWifi"
        const val PERMISSION_CAMERA = "PermissionCamera"
        const val PERMISSION_MICROPHONE = "PermissionMicrophone"
        const val PERMISSION_DENIED = "PermissionDenied"
        const val SETUP_INFORMATION = "SetupInformation"
        const val CHILD_MONITOR = "ChildMonitor"
        // Parent Device
        const val PARENT_DEVICE_INFO = "ParentDeviceInfo"
        const val SERVICE_DISCOVERY = "ServiceDiscovery"
        const val PAIRING = "Pairing"
        const val CONNECTION_FAILED = "ConnectionFailed"
        const val ALL_DONE = "AllDone"
        const val CLIENT_LIVE_CAMERA = "ClientLiveCamera"
        const val CLIENT_DASHBOARD = "ClientDashboard"
        const val CLIENT_ACTIVITY_LOG = "ClientActivityLog"
    }
}
