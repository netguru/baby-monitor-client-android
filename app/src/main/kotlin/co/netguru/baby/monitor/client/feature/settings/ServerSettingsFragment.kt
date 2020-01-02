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
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import co.netguru.baby.monitor.client.feature.server.ServerViewModel
import kotlinx.android.synthetic.main.fragment_server_settings.*
import kotlinx.android.synthetic.main.fragment_server_settings.closeIbtn
import kotlinx.android.synthetic.main.fragment_server_settings.logoutProgressBar
import kotlinx.android.synthetic.main.fragment_server_settings.rateUsBtn
import kotlinx.android.synthetic.main.fragment_server_settings.secondPartTv
import kotlinx.android.synthetic.main.fragment_server_settings.settingsLogoutBtn
import kotlinx.android.synthetic.main.fragment_server_settings.version
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
        setupViews()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.resetState.observe(viewLifecycleOwner, Observer { resetState ->
            when (resetState) {
                is ResetState.InProgress -> setupResetButton(true)
                is ResetState.Failed -> setupResetButton(false)
                is ResetState.Completed -> handleAppReset()
            }
        })
    }

    private fun setupViews() {
        sendRecordingsSw.isChecked = viewModel.isUploadEnabled()

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
            viewModel.resetApp()
        }

        sendRecordingsSw.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setUploadEnabled(isChecked)
        }

        version.text =
            getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    private fun setupResetButton(resetInProgress: Boolean) {
        settingsLogoutBtn.apply {
            isClickable = !resetInProgress
            text = if (resetInProgress) "" else resources.getString(R.string.reset_the_app)
        }
        logoutProgressBar.isVisible = resetInProgress
    }

    private fun handleAppReset() {
        requireActivity().startActivity(
            Intent(activity, OnboardingActivity::class.java)
        )
        requireActivity().finish()
    }
}
