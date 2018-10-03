package co.netguru.baby.monitor.client.feature.client.home.switchbaby

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.netguru.baby.monitor.client.feature.client.home.ChildData

class ChildrenAdapter(
        val onChildSelected: (ChildData) -> Unit,
        val onNewChildSelected: () -> Unit
) : RecyclerView.Adapter<ChildViewHolder>() {

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
            ChildViewHolder.CHILD_DATA_TYPE
        } else {
            ChildViewHolder.NEW_CHILD_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            if (viewType == ChildViewHolder.CHILD_DATA_TYPE) {
                ChildDataHolder(parent, onChildSelected)
            } else {
                NewChildViewHolder(parent, onNewChildSelected)
            }

    override fun getItemCount() = childrenList.size

    override fun onBindViewHolder(viewHolder: ChildViewHolder, position: Int) {
        viewHolder.bindView(childrenList[position])
    }
}
