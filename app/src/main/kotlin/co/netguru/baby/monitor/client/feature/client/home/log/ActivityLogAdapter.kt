package co.netguru.baby.monitor.client.feature.client.home.log

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.DateProvider
import co.netguru.baby.monitor.client.common.view.StickyHeaderInterface
import co.netguru.baby.monitor.client.data.client.home.log.LogData
import co.netguru.baby.monitor.client.data.client.home.log.LogData.*
import co.netguru.baby.monitor.client.feature.client.home.log.LogsViewHolder.*
import org.threeten.bp.LocalDateTime

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
        activityList.add(EndText(activityList.lastOrNull()?.timeStamp ?: LocalDateTime.now()))
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (activityList[position]) {
        is Data -> R.layout.item_log_activity_record
        is LogHeader -> R.layout.item_log_activity_header
        is EndText -> R.layout.item_log_activity_end_text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                R.layout.item_log_activity_header -> HeaderViewHolder(parent, viewType)
                R.layout.item_log_activity_record -> DataLogsViewHolder(parent, viewType)
                else -> EndTextHolder(parent, viewType)
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
                map[activityList[itemPosition].timeStamp.toLocalDate().toString()] ?: 0
            }


    override fun bindHeaderData(header: View, headerPosition: Int) {
        val textView = header.findViewById(R.id.itemActivityLogHeaderTv) as TextView
        val data = activityList[headerPosition]
        if (data is LogHeader) {
            val today = DateProvider.midnight
            val yesterday = DateProvider.yesterdaysMidnight
            val baseText = data.timeStamp.format(DateProvider.headerFormatter)

            textView.text = when {
                data.timeStamp.isAfter(today) -> header.context.getString(R.string.date_today, baseText)
                data.timeStamp.isAfter(yesterday) -> header.context.getString(R.string.date_yesterday, baseText)
                else -> baseText
            }
        }
    }
}
