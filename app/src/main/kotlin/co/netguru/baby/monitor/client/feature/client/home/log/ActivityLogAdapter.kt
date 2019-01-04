package co.netguru.baby.monitor.client.feature.client.home.log

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.client.home.log.LogsViewHolder.DataLogsViewHolder
import co.netguru.baby.monitor.client.feature.client.home.log.LogsViewHolder.HeaderViewHolder
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogData
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogData.Data
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogData.LogHeader
import co.netguru.baby.monitor.client.feature.common.view.StickyHeaderInterface
import org.threeten.bp.format.DateTimeFormatter

class ActivityLogAdapter : RecyclerView.Adapter<LogsViewHolder>(), StickyHeaderInterface {

    private var map = hashMapOf<String, Int>()
    private var activityList = mutableListOf<LogData>()

    internal fun setupList(list: List<LogData>) {
        map = hashMapOf()
        activityList = mutableListOf()

        list.asSequence()
                .sortedByDescending { it.timeStamp }
                .forEach { data ->
                    with(data.timeStamp) {
                        if (!map.containsKey(toLocalDate().toString())) {
                            activityList.add(LogHeader(this))
                            map[toLocalDate().toString()] = activityList.lastIndex
                        }
                    }
                    if (!activityList.contains(data)) {
                        activityList.add(data)
                    }
                }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (activityList[position]) {
        is Data -> R.layout.item_log_activity_record
        is LogHeader -> R.layout.item_log_activity_header
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            if (viewType == R.layout.item_log_activity_header) {
                HeaderViewHolder(parent, viewType)
            } else {
                DataLogsViewHolder(parent, viewType)
            }

    override fun getItemCount() = activityList.size

    override fun onBindViewHolder(viewHolder: LogsViewHolder, position: Int) {
        viewHolder.bindView(activityList[position])
    }

    override fun isHeader(itemPosition: Int) = activityList[itemPosition] is LogHeader

    override fun getHeaderLayout(headerPosition: Int) = R.layout.item_log_activity_header

    override fun getHeaderPositionForItem(itemPosition: Int) =
            if (activityList[itemPosition] is LogHeader) {
                itemPosition
            } else {
                map[(activityList[itemPosition] as Data).timeStamp.toLocalDate().toString()] ?: 0
            }


    override fun bindHeaderData(header: View, headerPosition: Int) {
        val textView = header.findViewById(R.id.itemActivityLogHeaderTv) as TextView
        val data = activityList[headerPosition]
        if (data is LogHeader) {
            textView.text = data.timeStamp.format(headerFormatter)
        }
    }

    companion object {
        internal val headerFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        internal val timeStampFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")
    }
}
