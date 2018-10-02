package co.netguru.baby.monitor.client.feature.client.home.switch_baby

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import timber.log.Timber

class ChildrenAdapter(
        recyclerView: RecyclerView
) : RecyclerView.Adapter<ChildViewHolder>() {

    internal lateinit var onChildSelected: (ChildData) -> Unit

    internal var childrenList = listOf<ChildData>()
        set(value) {
            field = value
            notifyDataSetChanged()
            Timber.e("${value.size}")
        }

    init {
        recyclerView.adapter = this
        recyclerView.setHasFixedSize(true)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChildViewHolder(parent, onChildSelected)

    override fun getItemCount() = childrenList.size

    override fun onBindViewHolder(viewHolder: ChildViewHolder, position: Int) {
        viewHolder.bindView(childrenList[position])
    }
}
