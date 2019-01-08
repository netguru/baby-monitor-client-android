package co.netguru.baby.monitor.client.feature.common.view

import android.content.Context
import android.util.AttributeSet
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer

class CustomSurfaceViewRenderer(context: Context, attrs: AttributeSet?) : SurfaceViewRenderer(context, attrs) {

    internal var initialized = false

    constructor(context: Context) : this(context, null)

    override fun init(sharedContext: EglBase.Context?, rendererEvents: RendererCommon.RendererEvents?) {
        super.init(sharedContext, rendererEvents)
        initialized = true
    }
}
