package co.netguru.baby.monitor.client.common.extensions

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import co.netguru.baby.monitor.client.feature.common.Complete
import co.netguru.baby.monitor.client.feature.common.DataBounder
import co.netguru.baby.monitor.client.feature.common.Error
import co.netguru.baby.monitor.client.feature.common.Next
import io.reactivex.*

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun Completable.applyIoSchedulers() = this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun Completable.applyComputationSchedulers() = this.subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.applyIoSchedulers() = this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.applyComputationSchedulers() = this.subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.applyIoSchedulers() = this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.applyComputationSchedulers() = this.subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.applyIoSchedulers() = this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.applyComputationSchedulers() = this.subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.applyIoSchedulers(): Flowable<T> = this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.applyComputationSchedulers(): Flowable<T> =
        this.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())

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
