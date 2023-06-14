package co.netguru.baby.monitor.client.feature.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerParentActivityViewModel
import co.netguru.baby.monitor.client.databinding.FragmentVoiceRecordingsSettingBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import javax.inject.Inject
import javax.inject.Provider

class VoiceRecordingsSettingsFragment : BaseFragment(R.layout.fragment_voice_recordings_setting) {
    override val screen: Screen = Screen.VOICE_RECORDINGS_SETTING
    private lateinit var binding: FragmentVoiceRecordingsSettingBinding

    @Inject
    lateinit var viewModelProvider : Provider<ConfigurationViewModel>

    private val viewModel by daggerParentActivityViewModel { viewModelProvider }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVoiceRecordingsSettingBinding.inflate(layoutInflater)
        appComponent.inject(this)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            featureDNextBtn.setOnClickListener {
                findNavController().navigate(R.id.featureDToConnecting)
            }
            featureDSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.setUploadEnabled(isChecked)
            }
        }
    }
}
