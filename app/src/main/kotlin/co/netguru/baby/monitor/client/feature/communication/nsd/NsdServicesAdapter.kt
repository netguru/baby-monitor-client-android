package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdServiceInfo
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.netguru.baby.monitor.client.R

class NsdServicesAdapter(private val onServiceClick: (nsdServiceInfo: NsdServiceInfo) -> Unit) :
    ListAdapter<NsdServiceInfo, ServiceViewHolder>(NsdServiceDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = View.inflate(parent.context, R.layout.found_service_item, null)
        val layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            view.resources.getDimension(R.dimen.nsd_service_item_height).toInt()
        )
        view.layoutParams = layoutParams
        return ServiceViewHolder(view)
            .apply {
                itemView.setOnClickListener {
                    onServiceClick.invoke(getItem(adapterPosition))
                }
            }
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
