package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import kotlinx.android.synthetic.main.onboarding_buttons.*

class FeaturePresentationFragment : Fragment() {

    private var layoutResource = R.layout.fragment_feature_a

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layoutResource = when (arguments?.getString(FEATURE_KEY)) {
            FEATURE_B -> R.layout.fragment_feature_b
            FEATURE_C -> R.layout.fragment_feature_c
            FEATURE_D -> R.layout.fragment_feature_d
            else -> R.layout.fragment_feature_a
        }

        return inflater.inflate(layoutResource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (layoutResource == R.layout.fragment_feature_d) {
            featureNextBtn.text = getString(R.string.feature_lets_start)
            featureSkipBtn.text = getString(R.string.feature_maybe_later_btn)
        }

        featureNextBtn.setOnClickListener {
            handleNextClicked()
        }
        featureSkipBtn.setOnClickListener {
            findNavController().navigate(R.id.actionFeatureSkip)
        }
    }

    private fun handleNextClicked() {
        val nextFeature = when (layoutResource) {
            R.layout.fragment_feature_a -> FEATURE_B
            R.layout.fragment_feature_b -> FEATURE_C
            R.layout.fragment_feature_c -> FEATURE_D
            else -> ""
        }
        val bundle = Bundle().apply {
            putString(FEATURE_KEY, nextFeature)
        }
        findNavController().navigate(
                if (nextFeature.isEmpty()) {
                    R.id.actionFeatureSkip
                } else {
                    R.id.actionToFeature
                },
                bundle
        )
    }

    companion object {
        private const val FEATURE_KEY = "FEATURE_KEY"
        private const val FEATURE_B = "FEATURE_B"
        private const val FEATURE_C = "FEATURE_C"
        private const val FEATURE_D = "FEATURE_D"
    }
}
