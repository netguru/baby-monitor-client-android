package co.netguru.baby.monitor.client.feature.client.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.common.extensions.trimmedText
import co.netguru.baby.monitor.client.data.server.NsdServiceManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_configuration.*
import javax.inject.Inject

class ConfigurationFragment : DaggerFragment() {

    @Inject
    internal lateinit var nsdServiceManager: NsdServiceManager

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_configuration, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDiscoveringButton.setOnClickListener {
            showProgressBar(true)
            nsdServiceManager.discoverService(object :
                    NsdServiceManager.OnServiceConnectedListener {
                override fun onServiceConnected() {
                    nsdServiceManager.stopServiceDiscovery()
                    findNavController().navigate(R.id.actionConfigurationClientHome)
                    requireActivity().finish()
                }

                override fun onServiceConnectionError() {
                    showProgressBar(false)
                    showSnackbarMessage(R.string.discovering_services_error)
                }
            })
        }
        setupDebugViews()
    }

    private fun setupDebugViews() {
        if (BuildConfig.DEBUG) {
            debugAddressGroup.setVisible(true)
            debugSetAddressButton.setOnClickListener {
                if (!debugAddressEt.text.isNullOrEmpty()) {
                    nsdServiceManager.appendNewAddress(debugAddressEt.trimmedText)
                    findNavController().navigate(R.id.actionConfigurationClientHome)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun showProgressBar(isVisible: Boolean) {
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
        startDiscoveringButton.visibility = if (!isVisible) View.VISIBLE else View.GONE
    }
}
