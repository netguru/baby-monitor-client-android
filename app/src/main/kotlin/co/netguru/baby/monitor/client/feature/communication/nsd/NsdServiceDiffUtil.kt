package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdServiceInfo
import androidx.recyclerview.widget.DiffUtil

class NsdServiceDiffUtil : DiffUtil.ItemCallback<NsdServiceInfo>() {
    override fun areItemsTheSame(oldItem: NsdServiceInfo, newItem: NsdServiceInfo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NsdServiceInfo, newItem: NsdServiceInfo): Boolean {
        return oldItem.host == newItem.host
    }
}
