package co.netguru.baby.monitor.client.feature.debug

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import co.netguru.baby.monitor.client.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.debug_view.view.*

class DebugView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(
        context,
        attrs,
        attributeSetId
    )

    private var disposable: Disposable? = null

    init {
        View.inflate(context, R.layout.debug_view, this).apply {
            orientation = VERTICAL
        }
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

    private fun setDebugState(debugState: DebugState) {
        cryingProbability.text = "$CRYING_PROBABILITY_PREFIX ${debugState.cryingProbability}"
        notificationInformation.text =
            "$NOTIFICATION_INFORMATION_PREFIX ${debugState.notificationInformation}"
    }

    companion object {
        private const val CRYING_PROBABILITY_PREFIX = "Crying probability:"
        private const val NOTIFICATION_INFORMATION_PREFIX = "Notification information:"
    }
}
