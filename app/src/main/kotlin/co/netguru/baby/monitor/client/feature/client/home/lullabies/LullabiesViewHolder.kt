package co.netguru.baby.monitor.client.feature.client.home.lullabies

import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_lullaby.*
import kotlinx.android.synthetic.main.item_lullaby_header.*

abstract class LullabiesViewHolder(
        parent: ViewGroup,
        layoutResource: Int
) : BaseViewHolder<LullabyData>(
        LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)
) {
    companion object {
        internal const val HEADER_VIEW = 0
        internal const val DATA_VIEW = 1
    }
}

class LullabiesHeaderHolder(
        parent: ViewGroup
) : LullabiesViewHolder(parent, R.layout.item_lullaby_header) {

    override fun bindView(item: LullabyData) {
        itemLullabyHeaderTv.text = item.name
    }
}

class LullabiesDataHolder(
        parent: ViewGroup,
        onLullabyPlayPressed: (LullabyData) -> Unit
) : LullabiesViewHolder(parent, R.layout.item_lullaby) {

    private var data: LullabyData? = null

    init {
        lullabyPlayCrl.setOnClickListener {
            data?.let(onLullabyPlayPressed)
        }
    }

    override fun bindView(item: LullabyData) = with(item as LullabyData.LullabyInfo) {
        data = this
        lullabyTitleTv.text = name
        lullabyDurationTv.text = duration
    }
}
