package co.netguru.baby.monitor.client.feature.client.home.switch_baby

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import co.netguru.baby.monitor.client.feature.common.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_child.*

class ChildViewHolder(
        parent: ViewGroup,
        private val onChildSelected: (ChildData) -> Unit
) : BaseViewHolder<ChildData>(
        LayoutInflater.from(parent.context).inflate(R.layout.item_child, parent, false)
) {

    override fun bindView(item: ChildData) {
        GlideApp.with(itemView.context)
                .load(item.image)
                .into(itemChildIv)

        itemChildNameTv.text = item.name

        itemChildContainerLl.setOnClickListener {
            onChildSelected(item)
        }

    }

}
