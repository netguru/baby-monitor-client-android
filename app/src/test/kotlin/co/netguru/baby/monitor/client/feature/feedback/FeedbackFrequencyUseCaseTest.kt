package co.netguru.baby.monitor.client.feature.feedback

import android.content.SharedPreferences
import co.netguru.baby.monitor.client.common.TimestampProvider
import co.netguru.baby.monitor.client.feature.feedback.FeedbackFrequencyUseCase.Companion.FEEDBACK_TIMESTAMP_KEY
import co.netguru.baby.monitor.client.feature.feedback.FeedbackFrequencyUseCase.Companion.NOTIFICATION_COUNTER_KEY
import co.netguru.baby.monitor.client.feature.feedback.FeedbackFrequencyUseCase.Companion.NO_FEEDBACK_COUNTER_LIMIT
import co.netguru.baby.monitor.client.feature.feedback.FeedbackFrequencyUseCase.Companion.NO_FEEDBACK_REQUEST_HOURS
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class FeedbackFrequencyUseCaseTest {

    private val sharedPreferencesEditor = mock<SharedPreferences.Editor>()
    private val sharedPreferences = mock<SharedPreferences> {
        on { edit() } doReturn sharedPreferencesEditor
    }
    private val timestamp = 1L
    private val counter = 1
    private val timestampProvider = mock<TimestampProvider> {
        on { timestamp() } doReturn timestamp
    }

    private val feedbackFrequencyUseCase =
        FeedbackFrequencyUseCase(sharedPreferences, timestampProvider)

    @Test
    fun `should increment counter on notification sent`() {
        whenever(sharedPreferences.getInt(NOTIFICATION_COUNTER_KEY, 0)) doReturn counter

        feedbackFrequencyUseCase.notificationSent()

        verify(sharedPreferencesEditor).putInt(NOTIFICATION_COUNTER_KEY, counter + 1)
    }

    @Test
    fun `should update timestamp on feedback request`() {
        feedbackFrequencyUseCase.feedbackRequestSent()

        verify(sharedPreferencesEditor).putLong(FEEDBACK_TIMESTAMP_KEY, timestamp)
    }

    @Test
    fun `should clear counter on feedback request`() {
        feedbackFrequencyUseCase.feedbackRequestSent()

        verify(sharedPreferencesEditor).putInt(NOTIFICATION_COUNTER_KEY, 0)
    }

    @Test fun `should ask for feedback when counter and time conditions are met`() {
        whenever(sharedPreferences.getInt(NOTIFICATION_COUNTER_KEY, 0)) doReturn NO_FEEDBACK_COUNTER_LIMIT + 1
        whenever(timestampProvider.timestamp()) doReturn TimeUnit.HOURS.toMillis(NO_FEEDBACK_REQUEST_HOURS) + 1

        assert(feedbackFrequencyUseCase.shouldAskForFeedback())
    }

    @Test fun `shouldn't ask for feedback when counter condition isn't met`() {
        whenever(sharedPreferences.getInt(NOTIFICATION_COUNTER_KEY, 0)) doReturn NO_FEEDBACK_COUNTER_LIMIT - 1

        assert(!feedbackFrequencyUseCase.shouldAskForFeedback())
    }

    @Test fun `shouldn't ask for feedback when time condition isn't met`() {
        whenever(timestampProvider.timestamp()) doReturn TimeUnit.HOURS.toMillis(NO_FEEDBACK_REQUEST_HOURS) - 1

        assert(!feedbackFrequencyUseCase.shouldAskForFeedback())
    }
}
