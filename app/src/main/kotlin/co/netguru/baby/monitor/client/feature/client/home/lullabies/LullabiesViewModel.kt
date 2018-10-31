package co.netguru.baby.monitor.client.feature.client.home.lullabies

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import javax.inject.Inject

class LullabiesViewModel @Inject constructor() : ViewModel() {

    internal val lullabiesData = MutableLiveData<List<LullabyData>>().also {
        it.value = LullabyPlayer.lullabies
    }
}
