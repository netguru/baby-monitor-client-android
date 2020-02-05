package co.netguru.baby.monitor.client.feature.analytics

import android.app.Activity
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class AnalyticsManager(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun setCurrentScreen(activity: Activity, screen: Screen) {
        firebaseAnalytics.setCurrentScreen(activity, screen.screenName, null)
        Timber.d("$SCREEN ${screen.screenName}")
    }

    fun logEvent(event: Event) {
        when (event) {
            is Event.Simple -> firebaseAnalytics.logEvent(event.eventType.eventName, null)
            is Event.ParamEvent -> firebaseAnalytics.logEvent(
                event.eventType.eventName,
                bundleOf(event.param.first.paramName to event.param.second)
            )
        }
        Timber.d("$EVENT ${event.eventType.eventName}")
    }

    fun setUserProperty(userProperty: UserProperty) {
        firebaseAnalytics.setUserProperty(userProperty.key, userProperty.value)
    }

    companion object {
        private const val EVENT = "event"
        private const val SCREEN = "screen"
    }
}
