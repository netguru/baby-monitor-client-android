package co.netguru.baby.monitor.client.feature.communication.pairing

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.feature.analytics.Screen
import kotlinx.android.synthetic.main.fragment_pairing.*
import java.net.URI
import javax.inject.Inject

class PairingFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_pairing
    override val screen: Screen = Screen.PAIRING

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProvider(this, factory)[PairingViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupViews()
        handlePairingCodeArgument()
    }

    private fun handlePairingCodeArgument() {
        arguments?.getString(ServiceDiscoveryFragment.ADDRESS_BUNDLE_KEY)?.let {
            viewModel.pair(URI.create(it))
        }
    }

    private fun setupViews() {
        pairingCode.text = viewModel.randomPairingCode
        setupOnBackPressedHandling()
    }

    private fun setupOnBackPressedHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.cancelPairing()
                findNavController().popBackStack()
            }
        })
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupObservers() {
        viewModel.pairingCompletedState.observe(viewLifecycleOwner,
            Observer { onConnectionCompleted(it) })
    }

    private fun onConnectionCompleted(connectionCompleted: Boolean) {
        findNavController().navigate(
            if (connectionCompleted) {
                R.id.pairingToAllDone
            } else {
                R.id.pairingToConnectionFailed
            }
        )
    }
}
