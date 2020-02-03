package co.netguru.baby.monitor.client.data.splash

import android.content.SharedPreferences
import co.netguru.baby.monitor.client.application.di.ConfigurationPreferencesQualifier
import co.netguru.baby.monitor.client.common.extensions.edit
import javax.inject.Inject

class AppStateHandler @Inject constructor(
    @ConfigurationPreferencesQualifier private val prefs: SharedPreferences
) {
    internal var appState: AppState
        get() = AppState.valueOf(
            prefs.getString(APP_STATE_KEY, null) ?: AppState.FIRST_OPEN.toString()
        )
        set(value) {
            prefs.edit {
                putString(APP_STATE_KEY, value.toString())
            }
        }

    companion object {
        private const val APP_STATE_KEY = "APP_STATE_KEY"
    }
}

enum class AppState {
    UNDEFINED, SERVER, CLIENT, FIRST_OPEN
}
