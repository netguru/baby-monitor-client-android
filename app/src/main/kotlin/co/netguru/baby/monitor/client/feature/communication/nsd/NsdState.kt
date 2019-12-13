package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdServiceInfo

sealed class NsdState {
    data class InProgress(
        val serviceInfoList: List<NsdServiceInfo>
    ) : NsdState()

    data class Error(val throwable: Throwable) : NsdState()

    data class Completed(
        val serviceInfoList: List<NsdServiceInfo>
    ) : NsdState()
}
