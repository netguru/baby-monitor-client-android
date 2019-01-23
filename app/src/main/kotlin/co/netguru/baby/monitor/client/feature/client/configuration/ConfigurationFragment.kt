package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_configuration.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConfigurationFragment : BaseDaggerFragment(), NsdServiceManager.OnServiceConnectedListener {
    override val layoutResource = R.layout.fragment_configuration

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private var timeOutDisposable: Disposable? = null
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setTimeOutForConnecting()
        viewModel.appSavedState.observe(this, Observer(this::handleAppState))
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopNsdServiceDiscovery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeOutDisposable?.dispose()
    }

    override fun onServiceConnectionError(errorCode: Int) {
        when (errorCode) {
            WifiP2pManager.P2P_UNSUPPORTED -> {
                showSnackbarMessage(R.string.p2p_unsupported_on_device_error)
            }
            WifiP2pManager.BUSY -> {
                showSnackbarMessage(R.string.system_is_busy_error)
            }
            else -> {
                showSnackbarMessage(R.string.discovering_services_error)
            }
        }
    }

    override fun onRegistrationFailed(errorCode: Int) {
        showSnackbarMessage(R.string.nsd_service_registration_failed)
    }

    override fun onStartDiscoveryFailed(errorCode: Int) {
        showSnackbarMessage(R.string.discovery_start_failed)
    }

    private fun setupView() {
        showProgressBar(true)
        viewModel.discoverNsdService(this)

        configurationBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setTimeOutForConnecting() {
        timeOutDisposable = Completable.timer(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe { findNavController().navigate(R.id.actionConfigurationToFailed) }
    }

    private fun showProgressBar(isVisible: Boolean) {
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun handleAppState(state: AppState?) {
        Timber.e("$state")
        val destination = when(state) {
            AppState.CLIENT -> R.id.configurationToDashboard
            else -> R.id.actionConfigurationConnectingDone
        }
        findNavController().navigate(destination)
    }

    companion object {
        const val TIMEOUT_IN_SECONDS = 20L
    }
}
