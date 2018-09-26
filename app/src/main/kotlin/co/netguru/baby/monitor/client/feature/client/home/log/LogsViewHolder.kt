package co.netguru.baby.monitor.client.feature.client.home.log

import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.extensions.getColorCompat
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData
import co.netguru.baby.monitor.client.feature.client.home.log.data.LogActivityData.LogData
import com.bumptech.glide.request.RequestOptions

sealed class LogsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bindView(logActivityData: LogActivityData)

    class DataPresenter(view: View) : LogsViewHolder(view) {

        private val actionIv = view.findViewById(R.id.itemActivityLogIv) as ImageView
        private val actionTv = view.findViewById(R.id.itemActivityLogActionTv) as TextView
        private val actionTimestampTv = view.findViewById(R.id.itemActivityLogActionTimestampTv) as TextView

        override fun bindView(logActivityData: LogActivityData) {
            if (logActivityData is LogData) {
                actionTv.text = logActivityData.action
                actionTimestampTv.text = logActivityData.timeStamp.format(ActivityLogAdapter.timeStampFormatter)
            }

            GlideApp
                    .with(itemView.context)
                    .load(ColorDrawable(itemView.context.getColorCompat(R.color.place_holder_grey)))
                    .apply(RequestOptions.circleCropTransform())
                    .into(actionIv)
        }
    }

    class HeaderPresenter(view: View) : LogsViewHolder(view) {

        private val textView = view.findViewById(R.id.itemActivityLogHeaderTv) as TextView

        override fun bindView(logActivityData: LogActivityData) {
            textView.text = logActivityData.timeStamp.format(ActivityLogAdapter.headerFormatter)
        }

    }
}
