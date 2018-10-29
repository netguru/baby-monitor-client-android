package co.netguru.baby.monitor.client.feature.client.home.lullabies

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import co.netguru.baby.monitor.client.feature.websocket.Action
import co.netguru.baby.monitor.client.feature.websocket.LullabyCommand
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LullabiesViewModel @Inject constructor() : ViewModel() {

    internal val lullabiesData = MutableLiveData<List<LullabyData>>().apply {
        postValue(LullabyPlayer.lullabies)
    }
    private val compositeDisposable = CompositeDisposable()

    fun handleCommand(command: LullabyCommand) = Single.just(lullabiesData.value).map { list ->
        list.forEachIndexed { index, lullaby ->
            if (lullaby.name == command.lullabyName) {
                (list[index] as LullabyData.LullabyInfo).action = command.action
            } else {
                if (lullaby is LullabyData.LullabyInfo) {
                    lullaby.action = Action.STOP
                }
            }
        }
        return@map list
    }.subscribeOn(Schedulers.io())
            .subscribeBy(onSuccess = { list ->
                lullabiesData.postValue(list)
            })
            .addTo(compositeDisposable)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
