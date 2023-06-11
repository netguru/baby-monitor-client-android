package co.netguru.baby.monitor.client.feature.communication.nsd

import android.net.nsd.NsdServiceInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.databinding.FoundServiceItemBinding

class NsdServicesAdapter(private val onServiceClick: (nsdServiceInfo: NsdServiceInfo) -> Unit) :
    ListAdapter<NsdServiceInfo, ServiceViewHolder>(NsdServiceDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = FoundServiceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            parent.resources.getDimension(R.dimen.nsd_service_item_height).toInt()
        )
        binding.root.layoutParams = layoutParams
        return ServiceViewHolder(binding)
            .apply {
                binding.root.setOnClickListener {
                    onServiceClick.invoke(getItem(adapterPosition))
                }
            }
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
