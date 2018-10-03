package co.netguru.baby.monitor.client.common.extensions

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import co.netguru.baby.monitor.client.feature.common.Complete
import co.netguru.baby.monitor.client.feature.common.DataBounder
import co.netguru.baby.monitor.client.feature.common.Error
import co.netguru.baby.monitor.client.feature.common.Next
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

fun <T> Observable<T>.subscribeWithLiveData(): LiveData<DataBounder<T>> {
    val liveData = MutableLiveData<DataBounder<T>>()
    this.subscribe(object : Observer<T> {
        override fun onComplete() {
            liveData.postValue(Complete())
        }

        override fun onSubscribe(d: Disposable) = Unit

        override fun onNext(t: T) {
            liveData.postValue(Next(t))
        }

        override fun onError(e: Throwable) {
            liveData.postValue(Error(e))
        }

    })
    return liveData
}
