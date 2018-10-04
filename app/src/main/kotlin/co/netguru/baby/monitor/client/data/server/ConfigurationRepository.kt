package co.netguru.baby.monitor.client.data.server

import android.content.SharedPreferences
import co.netguru.baby.monitor.client.application.ConfigurationPreferencesQualifier
import co.netguru.baby.monitor.client.common.extensions.edit
import co.netguru.baby.monitor.client.common.extensions.toData
import co.netguru.baby.monitor.client.common.extensions.toJson
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ConfigurationRepository @Inject constructor(
        @ConfigurationPreferencesQualifier private val preferences: SharedPreferences
) {

    internal var childrenList: List<ChildData>
        set(value) {

            preferences.edit {
                putString(CHILD_LIST_KEY, value.toJson())
            }
        }
        get() = preferences.getString(CHILD_LIST_KEY, "")
                ?.toData<Array<ChildData>>()
                ?.toList() ?: emptyList()

    internal fun appendChildrenList(data: ChildData) =
            if (childrenList.find { it.serverUrl == data.serverUrl } == null) {
                childrenList = childrenList.toMutableList().apply { add(data) }
                true
            } else {
                false
            }

    internal fun updateChildData(newData: ChildData?) {
        newData ?: return
        val index = childrenList.indexOfFirst { it.serverUrl == newData.serverUrl }
        if (index != -1) {
            childrenList = childrenList.toMutableList().apply { this[index] = newData }
        }
    }

    companion object {
        private const val CHILD_LIST_KEY = "key:child_list"
    }
}
