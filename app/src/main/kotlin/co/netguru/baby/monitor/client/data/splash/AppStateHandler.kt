package co.netguru.baby.monitor.client.data.splash

import android.content.Context
import co.netguru.baby.monitor.client.common.extensions.edit
import javax.inject.Inject


class AppStateHandler @Inject constructor(
        context: Context
) {
    internal var appState: AppState
        get() = AppState.valueOf(
                prefs.getString(APP_STATE_KEY, null) ?: AppState.UNDEFINED.toString()
        )
        set(value) {
            prefs.edit {
                putString(APP_STATE_KEY, value.toString())
            }
        }

    private val prefs = context.getSharedPreferences(APP_STATE_HANDLER_PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val APP_STATE_HANDLER_PREFS = "APP_STATE_HANDLER_PREFS"
        private const val APP_STATE_KEY = "APP_STATE_KEY"
    }
}

enum class AppState {
    UNDEFINED, SERVER, CLIENT
}
