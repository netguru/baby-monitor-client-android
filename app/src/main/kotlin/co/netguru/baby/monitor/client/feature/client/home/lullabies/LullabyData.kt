package co.netguru.baby.monitor.client.feature.client.home.lullabies

import co.netguru.baby.monitor.client.common.extensions.subscribeWithLiveData
import io.reactivex.SingleSource
import io.reactivex.internal.operators.single.SingleDefer
import io.reactivex.schedulers.Schedulers

sealed class LullabyData {

    abstract val name: String

    data class LullabyInfo(
            override val name: String,
            val duration: String,
            val source: String
    ) : LullabyData()

    data class LullabyHeader(
            override val name: String
    ) : LullabyData()

    companion object {
        //todo remove when proper data loading will exist
        fun getSampleData() = SingleDefer.defer {
            SingleSource<List<LullabyData>> {
                val list = mutableListOf<LullabyData>()
                list.add(LullabyData.LullabyHeader("BM library"))
                for (i in 0..10) {
                    list.add(LullabyInfo("name $i", "$i:00 mins", ""))
                }
                list.add(LullabyData.LullabyHeader("Your library"))
                for (i in 0..10) {
                    list.add(LullabyInfo("name $i", "$i:00 mins", ""))
                }
                it.onSuccess(list)
            }
        }.subscribeOn(Schedulers.io()).subscribeWithLiveData()
    }
}
