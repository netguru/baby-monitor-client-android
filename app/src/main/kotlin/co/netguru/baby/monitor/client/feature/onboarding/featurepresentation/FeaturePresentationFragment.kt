package co.netguru.baby.monitor.client.feature.onboarding.featurepresentation

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.feature.onboarding.FinishOnboardingUseCase
import co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.featurebindings.FeatureBinding
import co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.featurebindings.FragmentFeatureABinding
import co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.featurebindings.FragmentFeatureBBinding
import co.netguru.baby.monitor.client.feature.onboarding.featurepresentation.featurebindings.FragmentFeatureCBinding
import javax.inject.Inject

class FeaturePresentationFragment : Fragment() {

    var layoutResource = R.layout.fragment_feature_a

    @Inject
    lateinit var finishOnboardingUseCase: FinishOnboardingUseCase
    private lateinit var binding: FeatureBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent.inject(this)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return when (arguments?.getString(FEATURE_KEY)) {
            FEATURE_B -> {
                layoutResource = R.layout.fragment_feature_b
                val rootView =
                    co.netguru.baby.monitor.client.databinding.FragmentFeatureBBinding.inflate(
                        inflater,
                        container,
                        false
                    ).root
                binding = FragmentFeatureBBinding()
                (binding as FragmentFeatureBBinding).bind(rootView)
                rootView
            }

            FEATURE_C -> {
                layoutResource = R.layout.fragment_feature_c
                val rootView =
                    co.netguru.baby.monitor.client.databinding.FragmentFeatureCBinding.inflate(
                        inflater,
                        container,
                        false
                    ).root
                binding = FragmentFeatureCBinding()
                (binding as FragmentFeatureCBinding).bind(rootView)
                rootView
            }

            else -> {
                layoutResource = R.layout.fragment_feature_a
                val rootView =
                    co.netguru.baby.monitor.client.databinding.FragmentFeatureABinding.inflate(
                        inflater,
                        container,
                        false
                    ).root
                binding = FragmentFeatureABinding()
                (binding as FragmentFeatureABinding).bind(rootView)
                rootView
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tos)?.apply {
            text = HtmlCompat.fromHtml(
                getString(R.string.tos_confirmation),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
            movementMethod = LinkMovementMethod.getInstance()
        }

        binding.nextButton.setOnClickListener {
            handleNextClicked()
        }
        binding.skipButton.setOnClickListener {
            findNavController().navigate(finishOnboarding())
        }
    }

    private fun handleNextClicked() {
        val nextFeature = when (layoutResource) {
            R.layout.fragment_feature_a -> FEATURE_B
            R.layout.fragment_feature_b -> FEATURE_C
            else -> ""
        }
        val bundle = Bundle().apply {
            putString(FEATURE_KEY, nextFeature)
        }
        findNavController().navigate(
            if (nextFeature.isEmpty()) {
                finishOnboarding()
            } else {
                R.id.featureToFeature
            },
            bundle
        )
    }

    private fun finishOnboarding(): Int {
        finishOnboardingUseCase.finishOnboarding()
        return R.id.onboardingToInfoAboutDevices
    }

    companion object {
        private const val FEATURE_KEY = "FEATURE_KEY"
        private const val FEATURE_B = "FEATURE_B"
        private const val FEATURE_C = "FEATURE_C"
    }
}
