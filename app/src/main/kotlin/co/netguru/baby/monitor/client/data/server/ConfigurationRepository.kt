package co.netguru.baby.monitor.client.data.server

import android.content.SharedPreferences
import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.application.ConfigurationPreferencesQualifier
import co.netguru.baby.monitor.client.common.extensions.edit
import dagger.Reusable
import timber.log.Timber
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
        get() {
            val address = preferences.getString(ADDRESS_KEY, "") ?: ""
            Timber.e(address)
            return address
        }

    companion object {
        private const val ADDRESS_KEY = "key:address"
    }
}
