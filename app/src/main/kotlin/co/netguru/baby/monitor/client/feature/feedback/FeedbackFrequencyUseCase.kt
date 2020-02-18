package co.netguru.baby.monitor.client.feature.feedback

import android.content.SharedPreferences
import co.netguru.baby.monitor.client.application.di.FeedbackPreferencesQualifier
import co.netguru.baby.monitor.client.common.TimestampProvider
import co.netguru.baby.monitor.client.common.extensions.edit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FeedbackFrequencyUseCase @Inject constructor(
    @FeedbackPreferencesQualifier
    private val sharedPreferences: SharedPreferences,
    private val timestampProvider: TimestampProvider
) {
    fun shouldAskForFeedback(): Boolean {
        val currentCounter = sharedPreferences.getInt(NOTIFICATION_COUNTER_KEY, 0)
        val lastFeedbackTimestamp = sharedPreferences.getLong(FEEDBACK_TIMESTAMP_KEY, 0)
        return enoughTimePassed(lastFeedbackTimestamp) && currentCounter > NO_FEEDBACK_COUNTER_LIMIT
    }

    fun notificationSent() {
        incrementCounter()
    }

    fun feedbackRequestSent() {
        updateTimestamp()
        clearNotificationCounter()
    }

    private fun enoughTimePassed(lastFeedbackTimestamp: Long) =
        timestampProvider.timestamp() > lastFeedbackTimestamp + TimeUnit.HOURS.toMillis(
            NO_FEEDBACK_REQUEST_HOURS
        )

    private fun updateTimestamp() {
        sharedPreferences.edit {
            putLong(FEEDBACK_TIMESTAMP_KEY, timestampProvider.timestamp())
        }
    }

    private fun clearNotificationCounter() {
        sharedPreferences.edit {
            putInt(NOTIFICATION_COUNTER_KEY, CLEAR_COUNTER_VALUE)
        }
    }

    private fun incrementCounter() {
        val currentCounter = sharedPreferences.getInt(NOTIFICATION_COUNTER_KEY, 0)
        sharedPreferences.edit {
            putInt(NOTIFICATION_COUNTER_KEY, currentCounter + 1)
        }
    }

    companion object {
        internal const val FEEDBACK_TIMESTAMP_KEY = "feedback_timestamp"
        internal const val NOTIFICATION_COUNTER_KEY = "notification_counter"
        internal const val CLEAR_COUNTER_VALUE = 0
        internal const val NO_FEEDBACK_REQUEST_HOURS = 3L
        internal const val NO_FEEDBACK_COUNTER_LIMIT = 3
    }
}
