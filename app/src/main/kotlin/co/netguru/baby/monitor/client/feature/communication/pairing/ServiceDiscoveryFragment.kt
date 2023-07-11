package co.netguru.baby.monitor.client.feature.communication.pairing

import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.common.extensions.setDivider
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.databinding.FragmentConnectingDevicesBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServicesAdapter
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdState
import co.netguru.baby.monitor.client.feature.communication.nsd.ResolveFailedException
import co.netguru.baby.monitor.client.feature.communication.nsd.StartDiscoveryFailedException
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

class ServiceDiscoveryFragment : BaseFragment(R.layout.fragment_connecting_devices) {
    override val screen: Screen = Screen.SERVICE_DISCOVERY
    private lateinit var binding: FragmentConnectingDevicesBinding

    private val viewModel by daggerViewModel { viewModelProvider }

    @Inject
    internal lateinit var viewModelProvider: Provider<ServiceDiscoveryViewModel>

    private var timeOutDisposable: Disposable? = null
    private var nsdServicesAdapter: NsdServicesAdapter? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConnectingDevicesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupObservers()
        setupInProgressViews()
    }

    private fun setupAdapter() {
        nsdServicesAdapter =
            NsdServicesAdapter { nsdServiceInfo -> navigateToPairingFragment(nsdServiceInfo) }
        binding.recyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = nsdServicesAdapter
            setDivider(R.drawable.recycler_divider)
        }
    }

    private fun navigateToPairingFragment(nsdServiceInfo: NsdServiceInfo) {
        findNavController().navigate(
            R.id.serviceDiscoveryToPairing, bundleOf(
                ADDRESS_BUNDLE_KEY to "ws://${nsdServiceInfo.host.hostAddress}:${nsdServiceInfo.port}"
            )
        )
    }

    private fun setupObservers() {
        viewModel.nsdStateLiveData.observe(viewLifecycleOwner, Observer { nsdState ->
            handleNsdState(nsdState)
        })
    }

    private fun handleNsdState(nsdState: NsdState?) {
        when (nsdState) {
            is NsdState.Error -> handleNsdServiceError(nsdState.throwable)
            is NsdState.InProgress -> {
                if (binding.motionContainer.currentState != R.id.end) binding.motionContainer.transitionToEnd()
                handleServices(nsdState.serviceInfoList)
            }

            is NsdState.Completed -> {
                if (nsdState.serviceInfoList.isNotEmpty()) {
                    handleServices(nsdState.serviceInfoList)
                    setupCompleteViews()
                } else {
                    navigateToFailedConnection()
                }
            }

            else -> {}
        }
    }

    private fun handleNsdServiceError(throwable: Throwable) {
        when (throwable) {
            is ResolveFailedException -> showSnackbarMessage(R.string.discovering_services_error)
            is StartDiscoveryFailedException -> navigateToFailedConnection()
        }
    }

    private fun navigateToFailedConnection() {
        findNavController().navigate(R.id.serviceDiscoveryToConnectionFailed)
    }

    private fun setupCompleteViews() {
        with(binding) {
            cancelRefreshButton.apply {
                setOnClickListener {
                    discoverNsdService()
                }
                text = resources.getString(R.string.refresh_list)
            }
            backButton.apply {
                isVisible = true
                setOnClickListener {
                    findNavController().navigateUp()
                }
            }
            progressBar.isVisible = false
        }
    }

    private fun setupInProgressViews() {
        with(binding) {
            cancelRefreshButton.apply {
                setOnClickListener {
                    goBackToSpecifyDevice()
                }
                text = resources.getString(R.string.cancel)
            }
            backButton.isVisible = false
            progressBar.isVisible = true
        }
    }

    private fun handleServices(serviceInfoList: List<NsdServiceInfo>) {
        nsdServicesAdapter?.submitList(serviceInfoList.toMutableList())
    }

    private fun goBackToSpecifyDevice() {
        findNavController()
            .navigate(
                R.id.cancelServiceDiscovery, null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.specifyDevice, true)
                    .build()
            )
    }

    override fun onStart() {
        super.onStart()
        discoverNsdService()
    }

    private fun discoverNsdService() {
        val wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        viewModel.discoverNsdService(wifiManager)
        timeOutDisposable?.dispose()
        setupInProgressViews()
        timeOutDisposable = Single.timer(SEARCH_TIME_TILL_FAIL, TimeUnit.MINUTES)
            .subscribe { _ ->
                viewModel.stopNsdServiceDiscovery()
            }
    }

    override fun onStop() {
        timeOutDisposable?.dispose()
        viewModel.stopNsdServiceDiscovery()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        nsdServicesAdapter = null
    }

    companion object {
        private const val SEARCH_TIME_TILL_FAIL = 2L
        const val ADDRESS_BUNDLE_KEY = "addressKey"
    }
}
