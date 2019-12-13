package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdServiceInfo
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.found_service_item.view.*

class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(nsdServiceInfo: NsdServiceInfo) {
        itemView.deviceName.text =
            nsdServiceInfo.serviceName.removeSuffix(NsdServiceManager.SERVICE_NAME)
    }
}
