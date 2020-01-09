package co.netguru.baby.monitor.client.feature.analytics

import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class AnalyticsManager(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun setCurrentScreen(activity: Activity, screenName: String) {
        firebaseAnalytics.setCurrentScreen(activity, screenName, null)
        Timber.d("screen $screenName")
    }

    fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(eventName, params)
        Timber.d("event $eventName")
    }

    companion object {
        //Log events
        const val NOTIFICATION_SENT_EVENT = "notification_sent"
        const val TYPE_PARAM = "type"

        const val NOTIFICATION_SNOOZE_EVENT = "notification_snooze"

        const val RESET_APP_EVENT = "reset_app"

        const val NIGHT_MODE_EVENT = "night_mode"
        const val ENABLED_PARAM = "is_enabled"

        //Screen names
        //General
        const val SPLASH = "Splash"
        const val ONBOARDING = "Onboarding"
        const val INFO_ABOUT_DEVICES = "InfoAboutDevices"
        const val SPECIFY_DEVICE = "SpecifyDevice"
        //Child Device
        const val VOICE_RECORDINGS_SETTING = "VoiceRecordingsSetting"
        const val CONNECT_WIFI = "ConnectWifi"
        const val PERMISSION_CAMERA = "PermissionCamera"
        const val PERMISSION_MICROPHONE = "PermissionMicrophone"
        const val PERMISSION_DENIED = "PermissionDenied"
        const val SETUP_INFORMATION = "SetupInformation"
        const val CHILD_MONITOR = "ChildMonitor"
        //Parent Device
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
