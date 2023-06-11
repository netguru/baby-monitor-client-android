package co.netguru.baby.monitor.client.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.databinding.FragmentServerSettingsBinding
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageController
import co.netguru.baby.monitor.client.feature.server.ServerViewModel
import javax.inject.Inject
import javax.inject.Provider


//extends base frag
class ServerSettingsFragment : BaseFragment(R.layout.fragment_server_settings) {

    private lateinit var binding: FragmentServerSettingsBinding

    private val configurationViewModel : ConfigurationViewModel by daggerViewModel { configurationViewModelProvider  }
    private val serverViewModel : ServerViewModel by daggerViewModel { serverViewModelProvider }
    private val settingsViewModel : SettingsViewModel by daggerViewModel { settingsViegModelProvider }

    @Inject
    lateinit var configurationViewModelProvider : Provider<ConfigurationViewModel>

   @Inject
   lateinit var serverViewModelProvider: Provider<ServerViewModel>

   @Inject
    lateinit var settingsViegModelProvider: Provider<SettingsViewModel>


    override fun onAttach(context: Context) {
        appComponent.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServerSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupObservers() {
        configurationViewModel.resetState.observe(viewLifecycleOwner, Observer { resetState ->
            when (resetState) {
                is ChangeState.InProgress -> setupResetButton(true)
                is ChangeState.Failed -> setupResetButton(false)
                is ChangeState.Completed -> {}
            }
        })
    }

    private fun setupViews() {
        with(binding) {
            sendRecordingsSw.isChecked = configurationViewModel.isUploadEnabled()

            rateUsBtn.setOnClickListener {
                settingsViewModel.openMarket(requireActivity())
            }

            secondPartTv.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.company_url))
                    )
                )
            }

            closeIbtn.setOnClickListener {
                serverViewModel.toggleDrawer(false)
            }

            resetAppBtn.setOnClickListener {
                resetApp()
            }

            sendRecordingsSw.setOnCheckedChangeListener { _, isChecked ->
                configurationViewModel.setUploadEnabled(isChecked)
            }

            version.text =
                getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        }
    }

    private fun resetApp() {
        configurationViewModel.resetApp(requireActivity() as? MessageController)
    }

    private fun setupResetButton(resetInProgress: Boolean) {
        with(binding) {
            resetAppBtn.apply {
                isClickable = !resetInProgress
                text = if (resetInProgress) "" else resources.getString(R.string.reset)
            }
            resetProgressBar.isVisible = resetInProgress
        }
    }
}
