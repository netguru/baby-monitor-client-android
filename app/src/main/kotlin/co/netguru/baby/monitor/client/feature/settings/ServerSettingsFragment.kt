package co.netguru.baby.monitor.client.feature.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.feature.server.ServerViewModel
import kotlinx.android.synthetic.main.fragment_server_settings.*
import javax.inject.Inject

class ServerSettingsFragment : BaseDaggerFragment() {
    override val layoutResource = R.layout.fragment_server_settings

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            factory
        )[ConfigurationViewModel::class.java]
    }
    private val serverViewModel by lazy {
        ViewModelProviders.of(
            requireActivity(),
            factory
        )[ServerViewModel::class.java]
    }
    private val settingsViewModel by lazy {
        ViewModelProviders.of(
            this,
            factory
        )[SettingsViewModel::class.java]
    }

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
            serverViewModel.toggleDrawer(false)
        }

        settingsLogoutBtn.setOnClickListener {
            viewModel.resetApp(requireActivity())
        }

        sendRecordingsSw.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUploadEnabled(isChecked)
        }

        version.text =
            getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        viewModel.resetInProgress.observe(viewLifecycleOwner, Observer { resetInProgress ->
            settingsLogoutBtn.apply {
                isClickable = !resetInProgress
                text = if (resetInProgress) "" else resources.getString(R.string.reset_the_app)
            }
            logoutProgressBar.isVisible = resetInProgress
        })
    }
}
