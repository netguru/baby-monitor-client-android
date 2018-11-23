package co.netguru.baby.monitor.client.feature.common.view

import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation

object PresetedAnimations {
    fun getRotationAnimation(fromDegrees: Float, toDegrees: Float): RotateAnimation {
        return RotateAnimation(
                fromDegrees,
                toDegrees,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        ).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            fillAfter = true
            isFillEnabled = true
        }
    }
}
