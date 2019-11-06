package co.netguru.baby.monitor.client.feature.client.configuration

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.settings.ConfigurationViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_connecting_devices.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConnectingDevicesFragment : BaseDaggerFragment(),
    NsdServiceManager.OnServiceConnectedListener {
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
        viewModel.connectionCompletedState.observe(viewLifecycleOwner,
            Observer { onConnectionCompleted(it) })
        cancelSearchingButton.setOnClickListener {
            goBackToSpecifyDevice()
        }
    }

    private fun goBackToSpecifyDevice() {
        findNavController()
            .navigate(
                R.id.cancelConnecting, null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.specifyDevice, true)
                    .build()
            )
    }

    override fun onStart() {
        super.onStart()
        discoverNsdService()
        Single.timer(SEARCH_TIME_TILL_FAIL, TimeUnit.MINUTES)
            .subscribe { _ ->
                findNavController().navigate(R.id.connectionFailed)
            }
            .addTo(disposables)
    }

    private fun discoverNsdService() {
        val wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        viewModel.discoverNsdService(this, wifiManager)
    }

    override fun onStop() {
        disposables.clear()
        viewModel.stopNsdServiceDiscovery()
        super.onStop()
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

    private fun onConnectionCompleted(connectionCompleted: Boolean) {
        findNavController().navigate(
            if (connectionCompleted) {
                R.id.connectingDevicesToAllDone
            } else {
                R.id.connectionFailed
            }
        )
    }

    companion object {
        private const val SEARCH_TIME_TILL_FAIL = 2L
    }
}
