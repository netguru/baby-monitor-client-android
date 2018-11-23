package co.netguru.baby.monitor.client.feature.client.home.switchbaby

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.feature.client.home.ChildData
import co.netguru.baby.monitor.client.feature.common.view.BaseViewHolder
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_add_child.*
import kotlinx.android.synthetic.main.item_child.*

abstract class ChildViewHolder(parent: ViewGroup, layout: Int) : BaseViewHolder<ChildData>(
        LayoutInflater.from(parent.context).inflate(layout, parent, false)
) {
    companion object {
        internal const val CHILD_DATA_TYPE = 10
        internal const val NEW_CHILD_TYPE = 11
    }
}

class ChildDataHolder(
        parent: ViewGroup,
        private val onChildSelected: (ChildData) -> Unit
) : ChildViewHolder(parent, R.layout.item_child) {

    private lateinit var childData: ChildData

    init {
        itemChildContainerLl.setOnClickListener {
            onChildSelected(childData)
        }
    }

    override fun bindView(item: ChildData) = with(item) {
        GlideApp.with(itemView.context)
                .load(image)
                .apply(RequestOptions.circleCropTransform())
                .into(itemChildIv)

        itemChildNameTv.text = name
        childData = this
    }
}

class NewChildViewHolder(
        parent: ViewGroup,
        private val onNewChildSelected: () -> Unit
) : ChildViewHolder(parent, R.layout.item_add_child) {

    init {
        itemAddChildContainerLl.setOnClickListener {
            onNewChildSelected()
        }
    }

    override fun bindView(item: ChildData) = Unit
}

