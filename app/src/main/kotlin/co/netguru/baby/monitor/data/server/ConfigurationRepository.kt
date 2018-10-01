package co.netguru.baby.monitor.data.server

import android.content.SharedPreferences
import co.netguru.baby.monitor.application.App
import co.netguru.baby.monitor.application.ConfigurationPreferencesQualifier
import co.netguru.baby.monitor.common.extensions.edit
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ConfigurationRepository @Inject constructor(
        @ConfigurationPreferencesQualifier private val preferences: SharedPreferences
) {

    internal var serverAddress: String
        set(value) {
            preferences.edit {
                putString(ADDRESS_KEY, "rtsp://$value:${App.PORT}")
            }
        }
        get() = preferences.getString(ADDRESS_KEY, "") ?: ""

    companion object {
        private const val ADDRESS_KEY = "key:address"
    }
}
