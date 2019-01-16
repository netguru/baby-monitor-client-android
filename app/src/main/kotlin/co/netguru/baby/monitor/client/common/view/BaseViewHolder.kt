package co.netguru.baby.monitor.client.common.view

import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer

abstract class BaseViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

    override val containerView: View? = itemView

    abstract fun bindView(item: T)
}
