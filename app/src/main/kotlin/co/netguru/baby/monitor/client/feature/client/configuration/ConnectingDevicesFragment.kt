package co.netguru.baby.monitor.client.feature.client.configuration

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConnectingDevicesFragment : BaseDaggerFragment(), NsdServiceManager.OnServiceConnectedListener {
    override val layoutResource = R.layout.fragment_connecting_devices

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private var timeOutDisposable: Disposable? = null
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java]
    }

    private val disposables = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.discoverNsdService(this)
        viewModel.appSavedState.observe(this, Observer(this::handleAppState))
    }

    override fun onStart() {
        super.onStart()
        Single.timer(2, TimeUnit.MINUTES)
            .subscribe { _ ->
                findNavController().navigate(R.id.configurationFailed)
            }
            .addTo(disposables)
    }

    override fun onStop() {
        disposables.clear()
        super.onStop()
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

    private fun handleAppState(state: AppState?) {
        when (state) {
            AppState.CLIENT -> {
                findNavController().navigate(R.id.configurationToClientHome)
                requireActivity().finish()
            }
            else -> {
                findNavController().navigate(R.id.configurationToAllDone)
            }
        }
    }
}
