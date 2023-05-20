package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import kotlinx.android.synthetic.main.fragment_voice_recordings_setting.*
import javax.inject.Inject

class VoiceRecordingsSettingsFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_voice_recordings_setting
    override val screen: Screen = Screen.VOICE_RECORDINGS_SETTING

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProvider(this, factory)[ConfigurationViewModel::class.java] }

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
