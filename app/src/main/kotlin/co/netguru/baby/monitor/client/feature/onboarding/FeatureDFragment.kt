package co.netguru.baby.monitor.client.feature.onboarding

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.client.configuration.ConfigurationViewModel
import kotlinx.android.synthetic.main.fragment_feature_d.*
import javax.inject.Inject

class FeatureDFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_feature_d

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        featureDNextBtn.setOnClickListener {
            findNavController().navigate(R.id.featureDToConnecting)
        }
        featureDSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setUploadEnabled(isChecked)
        }
    }
}
