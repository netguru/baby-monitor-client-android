package co.netguru.baby.monitor.client.feature.settings


import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.feature.client.configuration.ConfigurationViewModel
import co.netguru.baby.monitor.client.feature.server.ServerViewModel
import kotlinx.android.synthetic.main.fragment_server_settings.*
import javax.inject.Inject


class ServerSettingsFragment : BaseDaggerFragment() {
    override val layoutResource = R.layout.fragment_server_settings

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy { ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java] }
    private val serverViewModel by lazy { ViewModelProviders.of(requireActivity(), factory)[ServerViewModel::class.java] }
    private val settingsViewModel by lazy { ViewModelProviders.of(this, factory)[SettingsViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendRecordingsSw.isChecked = viewModel.isUploadEnablad()

        rateUsBtn.setOnClickListener {
            settingsViewModel.openMarket(requireActivity())
        }

        secondPartTv.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.company_url))))
        }

        closeIbtn.setOnClickListener {
            serverViewModel.shouldDrawerBeOpen.postValue(false)
        }

        settingsLogoutBtn.setOnClickListener {
            viewModel.clearData(requireActivity())
        }

        sendRecordingsSw.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setUploadEnabled(isChecked)
        }
    }
}
