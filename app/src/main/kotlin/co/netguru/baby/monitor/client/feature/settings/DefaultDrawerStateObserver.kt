package co.netguru.baby.monitor.client.feature.settings

import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.DrawerLayout.STATE_DRAGGING
import android.support.v4.widget.DrawerLayout.STATE_IDLE
import android.view.View

open class DefaultDrawerObserver(
        private val onDrawerStateChanged: (state: Int) -> Unit = {},
        private val onDrawerSlide: (view: View, offset: Float) -> Unit = { view: View, offset: Float -> },
        private val onDrawerClosed: (view: View) -> Unit = {},
        private val onDrawerOpened: (view: View) -> Unit = {},
        private val onDrawerisClosing: () -> Unit = {},
        private val onDrawerisOpening: () -> Unit = {}
) : DrawerLayout.DrawerListener {
    private var lastState = STATE_IDLE
    private var wasOpen = false

    override fun onDrawerStateChanged(state: Int) {
        if (lastState == STATE_IDLE && state == STATE_DRAGGING) {
            if (wasOpen) {
                onDrawerisClosing.invoke()
            } else {
                onDrawerisOpening.invoke()
            }
        }
        lastState = state
        onDrawerStateChanged.invoke(state)
    }

    override fun onDrawerSlide(view: View, offset: Float) {
        onDrawerSlide.invoke(view, offset)
    }

    override fun onDrawerClosed(view: View) {
        wasOpen = false
        onDrawerClosed.invoke(view)
    }

    override fun onDrawerOpened(view: View) {
        wasOpen = true
        onDrawerOpened.invoke(view)
    }
}
