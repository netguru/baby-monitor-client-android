package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_configuration.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConfigurationFragment : DaggerFragment(), NsdServiceManager.OnServiceConnectedListener {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private var timeOutDisposable: Disposable? = null

    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_configuration, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setTimeOutForConnecting()

        viewModel.serviceInfoData.observe(this, Observer { service ->
            service ?: return@Observer

            viewModel.appendNewAddress(service.host.hostAddress, service.port) { success ->
                if (success) {
                    findNavController().navigate(R.id.actionConfigurationConnectingDone)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopNsdServiceDiscovery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeOutDisposable?.dispose()
    }

    override fun onServiceConnectionError() {
        showSnackbarMessage(R.string.discovering_services_error)
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

    companion object {
        const val TIMEOUT_IN_SECONDS = 20L
    }
}
