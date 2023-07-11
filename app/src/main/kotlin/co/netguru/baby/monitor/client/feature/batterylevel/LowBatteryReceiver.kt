package co.netguru.baby.monitor.client.feature.batterylevel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import javax.inject.Inject

class LowBatteryReceiver @Inject constructor(
    private val lowBatteryPublishSubject : LowBatteryPublishSubject
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_LOW)
            throw RuntimeException()
        lowBatteryPublishSubject.publishSubject.onNext(Unit)
    }

    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_LOW))
    }
}
