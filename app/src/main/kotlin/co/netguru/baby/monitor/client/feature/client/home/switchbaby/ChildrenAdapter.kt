package co.netguru.baby.monitor.client.feature.client.home.switchbaby

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import kotlin.properties.Delegates

class ChildrenAdapter(
        val onChildSelected: (ChildDataEntity) -> Unit,
        val onNewChildSelected: () -> Unit
) : RecyclerView.Adapter<ChildViewHolder>() {

    internal var selectedChild: ChildDataEntity? = null
    internal var originalList by Delegates.observable(
            emptyList<ChildDataEntity>(),
            onChange = { property, oldValue, newValue ->
                if (newValue.isNotEmpty() && selectedChild == null) {
                    selectedChild = newValue.first()
                }
                childrenList = newValue
            }
    )

    private var childrenList = listOf<ChildDataEntity>()
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
