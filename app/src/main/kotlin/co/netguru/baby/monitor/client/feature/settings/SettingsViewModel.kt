package co.netguru.baby.monitor.client.feature.settings

import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
        private val dataRepository: DataRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    fun updateChildName(name: String, data: ChildDataEntity) {
        dataRepository.updateChildData(data.apply { this.name = name })
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            Timber.i("data updated")
                        },
                        onError = Timber::e
                )
                .addTo(compositeDisposable)
    }

    fun saveImage(context: Context, cache: File, child: ChildDataEntity) {
        Single.fromCallable {
            val file = File(context.filesDir, cache.name)
            cache.copyTo(file, true)
            val previousPhoto = File(child.image ?: "")
            if (previousPhoto.exists()) {
                previousPhoto.delete()
            }
            file
        }.flatMapCompletable {
            dataRepository.updateChildData(child.apply { image = it.path })
        }.subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            Timber.i("data updated")
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
