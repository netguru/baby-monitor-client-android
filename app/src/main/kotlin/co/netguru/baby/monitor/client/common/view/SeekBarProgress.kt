package co.netguru.baby.monitor.client.common.view

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.settings.ChangeState
import kotlinx.android.synthetic.main.seek_bar_progress.view.*

class SeekBarProgress : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    init {
        View.inflate(context, R.layout.seek_bar_progress, this)
    }

    fun setState(valueState: Pair<ChangeState?, Int?>) {
        val changeState = valueState.first
        val value = valueState.second
        resolveVisibility(changeState)
        when (changeState) {
            ChangeState.Completed -> setAnimatedDrawable(true)
            ChangeState.Failed -> setAnimatedDrawable(false)
            ChangeState.InProgress -> Unit
            null -> {
                value?.let {
                    progressText.text = it.toString()
                }
            }
        }
    }

    private fun resolveVisibility(changeState: ChangeState?) {
        progressText.isVisible = changeState == null
        progress.isVisible = changeState == ChangeState.InProgress
        progressIcon.isVisible =
            changeState == ChangeState.Completed || changeState == ChangeState.Failed
    }

    private fun setAnimatedDrawable(success: Boolean) {
        val animatedVectorDrawable = AppCompatResources.getDrawable(
            context,
            if(success) R.drawable.animated_done else R.drawable.animated_fail
        ) as? AnimatedVectorDrawable
        animatedVectorDrawable?.let {
            progressIcon.setImageDrawable(it)
            it.start()
        }
    }
}
