package co.netguru.baby.monitor.client.feature.client.home.lullabies

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.view.StickyHeaderInterface
import co.netguru.baby.monitor.client.data.client.home.lullabies.LullabyData
import co.netguru.baby.monitor.client.data.communication.websocket.Action

class LullabiesAdapter(
        private val onLullabyPlayPressed: (name: String, action: Action) -> Unit
) : RecyclerView.Adapter<LullabiesViewHolder>(), StickyHeaderInterface {

    private var indexOfSecondHeader = 0
    internal var lullabies = emptyList<LullabyData>()
        /**
         * There will be only two headers, one at the beginning
         * for second one we search with indexOfLast
         */
        set(value) {
            field = value.also {
                indexOfSecondHeader = it.indexOfLast { it is LullabyData.LullabyHeader }
            }
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int) = when (lullabies[position]) {
        is LullabyData.LullabyInfo -> LullabiesViewHolder.DATA_VIEW
        is LullabyData.LullabyHeader -> LullabiesViewHolder.HEADER_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        LullabiesViewHolder.DATA_VIEW -> LullabiesDataHolder(parent, onLullabyPlayPressed)
        else -> LullabiesHeaderHolder(parent)
    }

    override fun getItemCount() = lullabies.size

    override fun onBindViewHolder(viewHolder: LullabiesViewHolder, position: Int) {
        viewHolder.bindView(lullabies[position])
    }

    override fun getHeaderPositionForItem(itemPosition: Int) =
            if (itemPosition < indexOfSecondHeader) 0 else indexOfSecondHeader


    override fun getHeaderLayout(headerPosition: Int) = R.layout.item_lullaby_header

    override fun bindHeaderData(header: View, headerPosition: Int) {
        val title = header.findViewById<TextView>(R.id.itemLullabyHeaderTv)
        title.text = lullabies[headerPosition].name
    }

    override fun isHeader(itemPosition: Int) = lullabies[itemPosition] is LullabyData.LullabyHeader
}
