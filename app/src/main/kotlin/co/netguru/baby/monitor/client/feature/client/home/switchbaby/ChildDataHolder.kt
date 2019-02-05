package co.netguru.baby.monitor.client.feature.client.home.switchbaby

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.common.view.BaseViewHolder
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_add_child.*
import kotlinx.android.synthetic.main.item_child.*

abstract class ChildViewHolder(parent: ViewGroup, layout: Int) : BaseViewHolder<ChildDataEntity>(
        LayoutInflater.from(parent.context).inflate(layout, parent, false)
) {
    companion object {
        internal const val CHILD_DATA_TYPE = 10
        internal const val NEW_CHILD_TYPE = 11
    }
}

class ChildDataHolder(
        parent: ViewGroup,
        private val onChildSelected: (ChildDataEntity) -> Unit
) : ChildViewHolder(parent, R.layout.item_child) {

    private lateinit var childData: ChildDataEntity

    init {
        itemChildContainerLl.setOnClickListener {
            onChildSelected(childData)
        }
    }

    override fun bindView(item: ChildDataEntity) = with(item) {
        val dataToLoad: Any? = if (image.isNullOrEmpty()) {
            R.drawable.logo
        } else {
            image
        }

        GlideApp.with(itemView.context)
                .load(dataToLoad)
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
            /* todo (15.01.2019) uncomment when adding new child will be needed
            onNewChildSelected()
            */
        }
    }

    override fun bindView(item: ChildDataEntity) = Unit
}

