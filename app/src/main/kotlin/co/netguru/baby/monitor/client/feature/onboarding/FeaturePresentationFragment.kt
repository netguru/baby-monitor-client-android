package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import androidx.core.text.HtmlCompat
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.analytics.Screen
import kotlinx.android.synthetic.main.onboarding_buttons.*
import javax.inject.Inject

class FeaturePresentationFragment : BaseFragment() {

    override var layoutResource = R.layout.fragment_feature_a
    override val screen: Screen = Screen.ONBOARDING
    @Inject
    lateinit var finishOnboardingUseCase: FinishOnboardingUseCase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layoutResource = when (arguments?.getString(FEATURE_KEY)) {
            FEATURE_B -> R.layout.fragment_feature_b
            FEATURE_C -> R.layout.fragment_feature_c
            else -> R.layout.fragment_feature_a
        }
        return inflater.inflate(layoutResource, container, false)
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
        featureNextBtn.setOnClickListener {
            handleNextClicked()
        }
        featureSkipBtn.setOnClickListener {
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
