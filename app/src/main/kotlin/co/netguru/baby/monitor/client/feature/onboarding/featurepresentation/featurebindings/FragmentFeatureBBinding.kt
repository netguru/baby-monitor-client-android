package co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.featurebindings

import android.view.View
import android.widget.Button

class FragmentFeatureBBinding : FeatureBinding {
    private lateinit var binding: co.netguru.baby.monitor.client.databinding.FragmentFeatureBBinding
    override fun bind(root: View) {
        binding = co.netguru.baby.monitor.client.databinding.FragmentFeatureBBinding.bind(root)
    }

    override val nextButton: Button
        get() = binding.onboardingLayout.featureNextBtn
    override val skipButton: Button
        get() = binding.onboardingLayout.featureSkipBtn
}