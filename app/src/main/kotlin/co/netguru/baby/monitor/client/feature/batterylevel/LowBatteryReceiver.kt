package co.netguru.baby.monitor.client.feature.batterylevel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import co.netguru.baby.monitor.client.feature.server.ChildMonitorFragmentModule
import javax.inject.Inject

class LowBatteryReceiver @Inject constructor(
    @ChildMonitorFragmentModule.LowBatteryHandler private val lowBatteryHandler: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_LOW)
            throw RuntimeException()
        lowBatteryHandler()
    }

    fun register(context: Context) {
        context.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_LOW))
    }
}
