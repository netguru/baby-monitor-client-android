package co.netguru.baby.monitor.client.common

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class DefaultServiceConnection(
        private val onServiceDisconnected: (name: ComponentName?) -> Unit,
        private val onServiceConnected: (name: ComponentName?, service: IBinder?) -> Unit
) : ServiceConnection {
    override fun onServiceDisconnected(name: ComponentName?) {
        onServiceDisconnected.invoke(name)
    }
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        onServiceConnected.invoke(name, service)
    }
}
