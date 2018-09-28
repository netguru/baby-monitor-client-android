package co.netguru.baby.monitor.client.feature.client.home.log

import android.support.v7.widget.RecyclerView
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
import org.threeten.bp.format.DateTimeFormatter

class ActivityLogAdapter(
        list: List<LogActivityData.LogData>
) : RecyclerView.Adapter<LogsViewHolder>(), StickyHeaderInterface {

    private var map = hashMapOf<String, Int>()
    private var activityList = mutableListOf<LogActivityData>()

    init {
        setupList(list)
    }

    fun setupList(list: List<LogActivityData.LogData>) {
        list.asSequence()
                .sortedByDescending { it.timeStamp }
                .forEach {
                    with(it.timeStamp) {
                        if (!map.containsKey(toLocalDate().toString())) {
                            activityList.add(LogHeader(this))
                            map[toLocalDate().toString()] = activityList.lastIndex
                        }
                    }
                    activityList.add(it)
                }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (activityList[position]) {
        is LogData -> R.layout.item_log_activity_record

        is LogHeader -> R.layout.item_log_activity_header
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            if (viewType == R.layout.item_log_activity_header) {
                HeaderPresenter(parent, viewType)
            } else {
                DataPresenter(parent, viewType)
            }

    override fun getItemCount() = activityList.size

    override fun onBindViewHolder(viewHolder: LogsViewHolder, position: Int) {
        viewHolder.bindView(activityList[position])
    }

    override fun isHeader(itemPosition: Int) = activityList[itemPosition] is LogHeader

    override fun getHeaderLayout(headerPosition: Int) = R.layout.item_log_activity_header

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        return if (activityList[itemPosition] is LogHeader) {
            itemPosition
        } else {
            map[(activityList[itemPosition] as LogData).timeStamp.toLocalDate().toString()] ?: 0
        }
    }

    override fun bindHeaderData(header: View, headerPosition: Int) {
        val textView = header.findViewById(R.id.itemActivityLogHeaderTv) as TextView
        val data = activityList[headerPosition]
        if (data is LogHeader) {
            textView.text = data.timeStamp.format(headerFormatter)
        }
    }

    companion object {
        val headerFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
        val timeStampFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm")
    }
}
