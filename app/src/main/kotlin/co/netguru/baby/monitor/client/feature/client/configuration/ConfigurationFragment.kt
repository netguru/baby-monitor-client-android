package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.extensions.setVisible
import co.netguru.baby.monitor.client.feature.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.feature.common.extensions.trimmedText
import co.netguru.baby.monitor.client.data.server.NsdServiceManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_configuration.*
import javax.inject.Inject

class ConfigurationFragment : DaggerFragment(), NsdServiceManager.OnServiceConnectedListener {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ConfigurationViewModel::class.java]
    }

    private val adapter by lazy {
        ServiceAdapter { serviceInfo ->
            viewModel.appendNewAddress(serviceInfo.host.hostAddress, serviceInfo.port) { success ->
                if (success) {
                    findNavController().navigate(R.id.actionConfigurationClientHome)
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_configuration, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupDebugViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopNsdServiceDiscovery()
    }

    override fun onServiceConnectionError() {
        showSnackbarMessage(R.string.discovering_services_error)
    }

    private fun setupView() {
        discoveredDevicesRv.adapter = adapter
        showProgressBar(true)
        viewModel.discoverNsdService(this)
    }

    private fun setupDebugViews() {
        if (BuildConfig.DEBUG) {
            debugAddressGroup.setVisible(true)
            debugSetAddressButton.setOnClickListener {
                if (!debugAddressEt.text.isNullOrEmpty()) {
                    val trimmed = debugAddressEt.trimmedText.split(":")
                    viewModel.appendNewAddress(trimmed[0], trimmed[1].toInt()) {
                        findNavController().navigate(R.id.actionConfigurationClientHome)
                        requireActivity().finish()
                    }
                }
            }
        }
        viewModel.serviceInfoData.observe(this, Observer { servicesList ->
            adapter.list = servicesList ?: return@Observer
        })
    }

    private fun showProgressBar(isVisible: Boolean) {
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
