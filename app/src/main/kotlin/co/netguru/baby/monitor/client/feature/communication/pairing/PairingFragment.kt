package co.netguru.baby.monitor.client.feature.communication.pairing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.databinding.FragmentPairingBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider

class PairingFragment : BaseFragment(R.layout.fragment_pairing) {
    override val screen: Screen = Screen.PAIRING
    private lateinit var binding: FragmentPairingBinding

    @Inject
    lateinit var viewModelProvider: Provider<PairingViewModel>

    private val viewModel by daggerViewModel { viewModelProvider }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPairingBinding.inflate(layoutInflater)

        return binding.root
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
        binding.pairingCode.text = viewModel.randomPairingCode
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
        binding.backButton.setOnClickListener {
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
