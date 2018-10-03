package co.netguru.baby.monitor.client.feature.client.home.log

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData.LogData
import co.netguru.baby.monitor.client.feature.common.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_log_activity_header.*
import kotlinx.android.synthetic.main.item_log_activity_record.*

abstract class LogsViewHolder(
        parent: ViewGroup,
        viewType: Int
) : BaseViewHolder<LogActivityData>(
        LayoutInflater.from(parent.context).inflate(viewType, parent, false)
) {

    class DataLogsViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType) {

        override fun bindView(logActivityData: LogActivityData) {
            if (logActivityData is LogData) {
                itemActivityLogActionTv.text = logActivityData.action
                itemActivityLogActionTimestampTv.text =
                        logActivityData.timeStamp.format(ActivityLogAdapter.timeStampFormatter)
            }
        }
    }

    class HeaderViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType) {

        override fun bindView(logActivityData: LogActivityData) {
            itemActivityLogHeaderTv.text =
                    logActivityData.timeStamp.format(ActivityLogAdapter.headerFormatter)
        }
    }
}
