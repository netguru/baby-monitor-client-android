package co.netguru.baby.monitor.client.feature.client.configuration

import android.net.nsd.NsdServiceInfo
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class ServiceAdapter(
        private val onServiceSelected: (NsdServiceInfo) -> Unit
) : RecyclerView.Adapter<ServiceViewHolder>() {

    internal var list = listOf<NsdServiceInfo>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ServiceViewHolder(parent, onServiceSelected)

    override fun getItemCount() = list.size

    override fun onBindViewHolder(viewHolder: ServiceViewHolder, position: Int) =
            viewHolder.bindView(list[position])
}
