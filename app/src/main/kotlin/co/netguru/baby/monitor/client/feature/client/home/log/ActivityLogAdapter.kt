package co.netguru.baby.monitor.client.feature.client.home.log

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.view.StickyHeaderInterface
import co.netguru.baby.monitor.client.feature.client.home.log.LogsViewHolder.DataPresenter
import co.netguru.baby.monitor.client.feature.client.home.log.LogsViewHolder.HeaderPresenter
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData.LogData
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData.LogHeader

class ActivityLogAdapter(
        list: List<LogActivityData>
) : RecyclerView.Adapter<LogsViewHolder>(), StickyHeaderInterface {

    var activityList: List<LogActivityData> = list
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (activityList[position]) {
            is LogData -> R.layout.item_log_activity_record

            is LogHeader -> R.layout.item_log_activity_header
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return if (viewType == R.layout.item_log_activity_header) {
            HeaderPresenter(view)
        } else {
            DataPresenter(view)
        }
    }

    override fun getItemCount() = activityList.size

    override fun onBindViewHolder(viewHolder: LogsViewHolder, position: Int) {
        viewHolder.bindView(activityList[position])
    }

    override fun isHeader(itemPosition: Int) = activityList[itemPosition] is LogHeader

    override fun getHeaderLayout(headerPosition: Int) = R.layout.item_log_activity_header

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var lastIndex = 0
        activityList.forEachIndexed { index, logActivityData ->
            if (logActivityData is LogHeader && index <= itemPosition) {
                lastIndex = index
            }
        }

        return lastIndex
    }

    override fun bindHeaderData(header: View, headerPosition: Int) {
        val textView = header.findViewById(R.id.itemActivityLogHeaderTv) as TextView
        val data = activityList[headerPosition]
        if (data is LogHeader) {
            textView.text = data.text
        }
    }

}
