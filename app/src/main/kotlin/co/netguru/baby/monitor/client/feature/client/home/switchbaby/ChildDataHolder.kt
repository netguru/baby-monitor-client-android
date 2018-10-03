package co.netguru.baby.monitor.client.feature.client.home.switchbaby

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import co.netguru.baby.monitor.client.feature.common.base.BaseViewHolder
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_add_child.*
import kotlinx.android.synthetic.main.item_child.*

sealed class ChildViewHolder(parent: ViewGroup, layout: Int) : BaseViewHolder<ChildData>(
        LayoutInflater.from(parent.context).inflate(layout, parent, false)
)

class ChildDataHolder(
        parent: ViewGroup,
        private val onChildSelected: (ChildData) -> Unit
) : ChildViewHolder(parent, R.layout.item_child) {

    override fun bindView(item: ChildData) {
        GlideApp.with(itemView.context)
                .load(item.image)
                .apply(RequestOptions.circleCropTransform())
                .into(itemChildIv)

        itemChildNameTv.text = item.name

        itemChildContainerLl.setOnClickListener {
            onChildSelected(item)
        }

    }

}

class NewChildViewHolder(
        parent: ViewGroup,
        private val onNewChildSelected: () -> Unit
): ChildViewHolder(parent, R.layout.item_add_child) {

    override fun bindView(item: ChildData) {
        itemAddChildContainerLl.setOnClickListener {
            onNewChildSelected.invoke()
        }
    }

}
