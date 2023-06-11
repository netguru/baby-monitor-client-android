package co.netguru.baby.monitor.client.common.view

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.databinding.SeekBarProgressBinding
import co.netguru.baby.monitor.client.feature.settings.ChangeState

class SeekBarProgress : RelativeLayout {

    private var binding : SeekBarProgressBinding
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    init {
        View.inflate(context, R.layout.seek_bar_progress, this)
        val inflater = LayoutInflater.from(context)
        binding = SeekBarProgressBinding.inflate(inflater, this)

    }

    fun setState(valueState: Pair<ChangeState?, Int?>) {
        val (changeState, value) = valueState
        resolveVisibility(changeState)
        when (changeState) {
            ChangeState.Completed -> setAnimatedDrawable(true)
            ChangeState.Failed -> setAnimatedDrawable(false)
            ChangeState.InProgress -> Unit
            null -> {
                value?.let {
                 binding.progressText.text = it.toString()
                }
            }
        }
    }

    private fun resolveVisibility(changeState: ChangeState?) {
        with(binding){
            progressText.isVisible = changeState == null
            progress.isVisible = changeState == ChangeState.InProgress
            progressIcon.isVisible =
                changeState == ChangeState.Completed || changeState == ChangeState.Failed
        }
    }

    private fun setAnimatedDrawable(success: Boolean) {
        val animatedVectorDrawable = AppCompatResources.getDrawable(
            context,
            if (success) R.drawable.animated_done else R.drawable.animated_fail
        ) as? AnimatedVectorDrawable
        animatedVectorDrawable?.let {
            binding.progressIcon.setImageDrawable(it)
            it.start()
        }
    }
}
