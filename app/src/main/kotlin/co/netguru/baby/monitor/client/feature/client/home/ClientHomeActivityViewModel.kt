package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class ClientHomeActivityViewModel : ViewModel() {

    fun getChildList(): LiveData<List<ChildSpinnerData>> {
        //TODO add proper data loading
        return MutableLiveData<List<ChildSpinnerData>>().also {
            it.postValue(listOf(ChildSpinnerData("", "Marysia"), ChildSpinnerData("", "Jaś")))
        }
    }

}