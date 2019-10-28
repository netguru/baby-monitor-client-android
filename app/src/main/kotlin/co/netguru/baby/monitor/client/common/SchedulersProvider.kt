package co.netguru.baby.monitor.client.common

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

interface ISchedulersProvider {
    fun io(): Scheduler = Schedulers.io()
    fun computation(): Scheduler = Schedulers.computation()
    fun mainThread(): Scheduler = AndroidSchedulers.mainThread()
}

class SchedulersProvider @Inject constructor() : ISchedulersProvider
