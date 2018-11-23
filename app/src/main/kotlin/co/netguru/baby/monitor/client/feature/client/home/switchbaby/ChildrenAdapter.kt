package co.netguru.baby.monitor.client.feature.client.home.switchbaby

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import kotlin.properties.Delegates

class ChildrenAdapter(
        val onChildSelected: (ChildData) -> Unit,
        val onNewChildSelected: () -> Unit
) : RecyclerView.Adapter<ChildViewHolder>() {

    internal var selectedChild: ChildData? = null
    internal var originalList by Delegates.observable(
            emptyList<ChildData>(),
            onChange = { property, oldValue, newValue ->
                if (newValue.isNotEmpty() && selectedChild == null) {
                    selectedChild = newValue.first()
                }
                childrenList = newValue
            }
    )

    private var childrenList = listOf<ChildData>()
        set(value) {
            field = value.filter { it.address != selectedChild?.address }
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return if (position == childrenList.size) {
            ChildViewHolder.NEW_CHILD_TYPE
        } else {
            ChildViewHolder.CHILD_DATA_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            if (viewType == ChildViewHolder.CHILD_DATA_TYPE) {
                ChildDataHolder(parent) { data ->
                    onChildSelected(data)
                    selectedChild = data
                    childrenList = originalList
                }
            } else {
                NewChildViewHolder(parent, onNewChildSelected)
            }

    override fun getItemCount() = childrenList.size + 1

    override fun onBindViewHolder(viewHolder: ChildViewHolder, position: Int) {
        if (position != childrenList.size) {
            viewHolder.bindView(childrenList[position])
        }
    }
}
