package co.netguru.baby.monitor.client.feature.client.home.log

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.DateProvider
import co.netguru.baby.monitor.client.common.view.BaseViewHolder
import co.netguru.baby.monitor.client.data.client.home.log.LogData
import kotlinx.android.synthetic.main.item_log_activity_header.*
import kotlinx.android.synthetic.main.item_log_activity_record.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

abstract class LogsViewHolder(
        val parent: ViewGroup,
        viewType: Int
) : BaseViewHolder<LogData>(
        LayoutInflater.from(parent.context).inflate(viewType, parent, false)
) {

    class DataLogsViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType) {

        override fun bindView(item: LogData) {
            val hourBefore = LocalDateTime.now().minusHours(1)
            if (item is LogData.Data) {
                itemActivityLogActionTv.text = item.action
                itemActivityLogActionTimestampTv.text = if (item.timeStamp.isAfter(hourBefore)) {
                    itemView.context.getString(R.string.minutes_ago, (HOUR_IN_MINUTES - hourBefore.until(item.timeStamp, ChronoUnit.MINUTES)))
                } else {
                    item.timeStamp.format(DateProvider.timeStampFormatter)
                }
            }
        }
    }

    class HeaderViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType) {

        override fun bindView(item: LogData) {
            val today = DateProvider.midnight
            val yesterday = DateProvider.yesterdaysMidnight
            val baseText = item.timeStamp.format(DateProvider.headerFormatter)

            itemActivityLogHeaderTv.text = when {
                item.timeStamp.isAfter(today) -> itemView.context.getString(R.string.date_today, baseText)
                item.timeStamp.isAfter(yesterday) -> itemView.context.getString(R.string.date_yesterday, baseText)
                else -> baseText
            }
        }
    }

    class EndTextHolder(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType) {
        override fun bindView(item: LogData) = Unit
    }

    companion object {
        const val HOUR_IN_MINUTES = 60
    }
}
