package co.netguru.baby.monitor.client.data

import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import co.netguru.baby.monitor.client.application.ConfigurationPreferencesQualifier
import co.netguru.baby.monitor.client.feature.common.extensions.edit
import co.netguru.baby.monitor.client.feature.common.extensions.toData
import co.netguru.baby.monitor.client.feature.common.extensions.toJson
import dagger.Reusable
import io.reactivex.Single
import javax.inject.Inject

@Reusable
class ChildRepository @Inject constructor(
        @ConfigurationPreferencesQualifier private val preferences: SharedPreferences
) {

    internal val childList = MutableLiveData<List<ChildData>>()

    internal fun setChildData(data: List<ChildData>) {
        preferences.edit {
            putString(CHILD_LIST_KEY, data.toJson())
        }
    }

    internal fun refreshChildData(): Single<List<ChildData>> = Single.fromCallable {
        val list =  preferences.getString(CHILD_LIST_KEY, "")
                ?.toData<Array<ChildData>>()
                ?.toList() ?: emptyList()
        childList.postValue(list)
        return@fromCallable list
    }

    internal fun appendChildrenList(childData: ChildData) =
            Single.just(childData).map { data ->

                val list = if (childList.value.isNullOrEmpty())  {
                    mutableListOf()
                } else {
                    childList.value as MutableList
                }

                if (list.find { it.address == data.address } == null) {
                    setChildData(list.toMutableList().apply { add(data) })
                    true
                } else {
                    false
                }
            }

    internal fun updateChildData(newData: ChildData): Single<ChildData> = Single.fromCallable {
        val list = (childList.value as MutableList?) ?: return@fromCallable null
        val index = list.indexOfFirst { it.address == newData.address }
        if (index != -1) {
            setChildData(
                    list.toMutableList().apply { this[index] = newData }
            )
        }
        newData
    }

    companion object {
        private const val CHILD_LIST_KEY = "key:child_list"
    }
}
