package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdServiceInfo
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import co.netguru.baby.monitor.client.R
import kotlinx.android.synthetic.main.found_service_item.view.*

class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(nsdServiceInfo: NsdServiceInfo) {
        val deviceName = nsdServiceInfo.serviceName.removeSuffix(NsdServiceManager.SERVICE_NAME)
        itemView.deviceName.text =
            if (deviceName.isNotEmpty()) deviceName else itemView.resources.getString(
                R.string.unknown_device
            )
    }
}
