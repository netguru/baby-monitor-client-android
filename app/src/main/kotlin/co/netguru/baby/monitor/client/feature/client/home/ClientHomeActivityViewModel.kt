package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class ClientHomeActivityViewModel : ViewModel() {

    fun getChildList(): LiveData<List<ChildData>> {
        //TODO add proper data loading
        return MutableLiveData<List<ChildData>>().also {
            it.postValue(listOf(
                    ChildData("", "Marysia", ""),
                    ChildData("", "Jaś", "")
            ))
        }
    }

}
