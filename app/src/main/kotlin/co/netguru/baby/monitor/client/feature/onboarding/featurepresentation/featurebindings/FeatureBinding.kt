package co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.featurebindings

import android.view.View
import android.widget.Button

interface FeatureBinding {
    fun bind(root: View)

    val nextButton: Button

    val skipButton: Button
}