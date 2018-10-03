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
                ?.toList() ?: arrayListOf()

    internal fun appendChildrenList(data: ChildData): Boolean {
        val list = mutableListOf<ChildData>()
         list.addAll(childrenList)
        return if (list.find { it.serverUrl == data.serverUrl } == null) {
            list.add(data)
            childrenList = list
            true
        } else {
            false
        }
    }

    internal fun updateChildData(newData: ChildData?) {
        newData ?: return
        val list = mutableListOf<ChildData>()
        for (child in childrenList) {
            if (child.serverUrl == newData.serverUrl) {
                list.add(newData)
            } else {
                list.add(child)
            }
        }

        childrenList = list
    }

    companion object {
        private const val CHILD_LIST_KEY = "key:child_list"
    }
}
