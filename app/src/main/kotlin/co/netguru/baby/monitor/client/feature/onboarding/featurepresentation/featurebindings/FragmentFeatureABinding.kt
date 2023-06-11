package co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.featurebindings

import android.view.View
import android.widget.Button

class FragmentFeatureABinding : FeatureBinding {
    private lateinit var binding: co.netguru.baby.monitor.client.databinding.FragmentFeatureABinding
    override fun bind(root: View) {
        binding = co.netguru.baby.monitor.client.databinding.FragmentFeatureABinding.bind(root)
    }

    override val nextButton: Button
        get() = binding.onboardingLayout.featureNextBtn
    override val skipButton: Button
        get() = binding.onboardingLayout.featureSkipBtn
}