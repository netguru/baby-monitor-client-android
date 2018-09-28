package co.netguru.baby.monitor.client.feature.client.home.log

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.getColorCompat
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData.LogData
import co.netguru.baby.monitor.client.feature.common.base.BaseViewHolder
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_log_activity_header.*
import kotlinx.android.synthetic.main.item_log_activity_record.*

sealed class LogsViewHolder(
        parent: ViewGroup,
        viewType: Int
) : BaseViewHolder<LogActivityData>(
        LayoutInflater.from(parent.context).inflate(viewType, parent, false)
) {

    class DataPresenter(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType), LayoutContainer {

        override fun bindView(logActivityData: LogActivityData) {
            if (logActivityData is LogData) {
                itemActivityLogActionTv.text = logActivityData.action
                itemActivityLogActionTimestampTv.text =
                        logActivityData.timeStamp.format(ActivityLogAdapter.timeStampFormatter)
            }

            GlideApp
                    .with(itemView.context)
                    .load(ColorDrawable(itemView.context.getColorCompat(R.color.place_holder_grey)))
                    .apply(RequestOptions.circleCropTransform())
                    .into(itemActivityLogIv)
        }
    }

    class HeaderPresenter(
            parent: ViewGroup,
            viewType: Int
    ) : LogsViewHolder(parent, viewType), LayoutContainer {

        override fun bindView(logActivityData: LogActivityData) {
            itemActivityLogHeaderTv.text =
                    logActivityData.timeStamp.format(ActivityLogAdapter.headerFormatter)
        }

    }
}
