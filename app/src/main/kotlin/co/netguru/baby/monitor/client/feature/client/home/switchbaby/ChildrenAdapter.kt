package co.netguru.baby.monitor.client.feature.client.home.switchbaby

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.home.ChildData

class ChildrenAdapter : RecyclerView.Adapter<ChildViewHolder>() {

    internal lateinit var onChildSelected: (ChildData) -> Unit
    internal lateinit var onNewChildSelected: () -> Unit

    internal var childrenList = listOf<ChildData>()
        set(value) {
            field = mutableListOf<ChildData>().apply {
                addAll(value)
                add(ChildData(""))
            }
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return if (position == childrenList.size) {
            R.layout.item_child
        } else {
            R.layout.item_add_child
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            if (viewType == R.layout.item_child) {
                ChildDataHolder(parent, onChildSelected)
            } else {
                NewChildViewHolder(parent, onNewChildSelected)
            }

    override fun getItemCount() = childrenList.size

    override fun onBindViewHolder(viewHolder: ChildViewHolder, position: Int) {
        viewHolder.bindView(childrenList[position])
    }
}
