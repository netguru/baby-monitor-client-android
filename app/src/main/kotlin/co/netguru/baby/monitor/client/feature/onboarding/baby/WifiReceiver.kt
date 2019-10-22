package co.netguru.baby.monitor.client.feature.onboarding.baby

import androidx.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import co.netguru.baby.monitor.client.data.communication.SingleEvent


class WifiReceiver : BroadcastReceiver() {

    internal val isWifiConnected = MutableLiveData<SingleEvent<Boolean>>()

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            val info: NetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
            isWifiConnected.postValue(SingleEvent(event = info.isConnected))
        }
    }

    companion object {
        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }
    }
}
