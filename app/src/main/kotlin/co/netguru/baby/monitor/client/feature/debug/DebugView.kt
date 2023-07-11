package co.netguru.baby.monitor.client.feature.debug

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.databinding.DebugViewBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class DebugView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    private var binding: DebugViewBinding

    private var disposable: Disposable? = null

    init {
        View.inflate(context, R.layout.debug_view, this).apply {
            orientation = VERTICAL
        }
        val inflater = LayoutInflater.from(context)
        binding = DebugViewBinding.inflate(inflater, this)

    }

    fun setDebugStateObservable(observable: Observable<DebugState>) {
        disposable = observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setDebugState(it)
            }
    }

    fun clearDebugStateObservable() {
        disposable?.dispose()
    }

    @SuppressLint("SetTextI18n")
    private fun setDebugState(debugState: DebugState) {
        with(binding) {
            cryingProbability.text = "$CRYING_PROBABILITY_PREFIX ${debugState.cryingProbability}"
            notificationInformation.text =
                "$NOTIFICATION_INFORMATION_PREFIX ${debugState.notificationInformation}"
            soundDecibels.text = "$SOUND_DECIBELS_PREFIX ${debugState.decibels}"
        }
    }

    companion object {
        private const val CRYING_PROBABILITY_PREFIX = "Crying probability:"
        private const val NOTIFICATION_INFORMATION_PREFIX = "Notification information:"
        private const val SOUND_DECIBELS_PREFIX = "Decibels:"
    }
}
