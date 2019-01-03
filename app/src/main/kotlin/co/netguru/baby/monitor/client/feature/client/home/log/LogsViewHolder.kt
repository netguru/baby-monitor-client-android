package co.netguru.baby.monitor.client.feature.client.home.log

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogData
import co.netguru.baby.monitor.client.feature.common.view.BaseViewHolder
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_log_activity_header.*
import kotlinx.android.synthetic.main.item_log_activity_record.*

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

        override fun bindView(logData: LogData) {
            if (logData is LogData.Data) {
                val dataToLoad: Any? = if (logData.image.isNullOrEmpty()) {
                    R.drawable.logo
                } else {
                    logData.image
                }

                GlideApp.with(parent.context)
                        .load(dataToLoad)
                        .apply(RequestOptions.circleCropTransform())
                        .into(itemActivityLogIv)


                itemActivityLogActionTv.text = logData.action
                itemActivityLogActionTimestampTv.text =
                        logData.timeStamp.format(ActivityLogAdapter.timeStampFormatter)
            }
        }
    }

    class HeaderViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType) {

        override fun bindView(logData: LogData) {
            itemActivityLogHeaderTv.text =
                    logData.timeStamp.format(ActivityLogAdapter.headerFormatter)
        }
    }
}
