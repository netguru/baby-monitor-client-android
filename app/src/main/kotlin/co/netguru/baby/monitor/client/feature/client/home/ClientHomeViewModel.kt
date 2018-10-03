package co.netguru.baby.monitor.client.feature.client.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.data.server.ConfigurationRepository
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class ClientHomeViewModel @Inject constructor(
        private val configurationRepository: ConfigurationRepository
) : ViewModel() {

    internal val selectedChild = MutableLiveData<ChildData>()
    internal val shouldHideNavbar = MutableLiveData<Boolean>()

    init {
        if (configurationRepository.childrenList.isNotEmpty()) {
            selectedChild.postValue(configurationRepository.childrenList.first())
        }
    }

    fun getChildrenList(): List<ChildData> = configurationRepository.childrenList

    fun updateChildName(name: String) = {
        selectedChild.value?.name = name
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }.toCompletable().subscribeOn(Schedulers.io()).subscribe { Timber.d("complete") }

    private fun updateChildImageSource(path: String) = {
        selectedChild.value?.image = path
        configurationRepository.updateChildData(selectedChild.value)
        selectedChild.postValue(selectedChild.value)
    }.toCompletable().subscribeOn(Schedulers.io()).subscribe { Timber.d("complete") }

    fun saveImage(context: Context, cache: File?) = Observable.defer {
        ObservableSource<Boolean> {
            if (cache == null) {
                it.onError(FileNotFoundException())
                return@ObservableSource
            }
            //TODO check photo orientation
            val file = File(context.filesDir, cache.name)
            try {
                cache.copyTo(file, true)
                val previousPhoto = File(selectedChild.value?.image)
                if (previousPhoto.exists()) {
                    previousPhoto.delete()
                }
                updateChildImageSource(file.absolutePath)
                it.onComplete()
            } catch (e: IOException) {
                e.printStackTrace()
                it.onError(e)
            }
        }
    }.subscribeOn(Schedulers.io()).subscribeWithLiveData()

}
