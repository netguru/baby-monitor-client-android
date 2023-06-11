package co.netguru.baby.monitor.client.feature.client.home.dashboard

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.GlideApp
import co.netguru.baby.monitor.client.common.PermissionResult
import co.netguru.baby.monitor.client.common.PermissionUtils
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.common.extensions.getColor
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.databinding.FragmentClientDashboardBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import javax.inject.Provider

class ClientDashboardFragment : BaseFragment(R.layout.fragment_client_dashboard) {
    override val screen: Screen = Screen.CLIENT_DASHBOARD
    private lateinit var binding: FragmentClientDashboardBinding

    private val viewModel by daggerViewModel { viewModelProvider }

    @Inject
    lateinit var viewModelProvider : Provider<ClientHomeViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentClientDashboardBinding.inflate(layoutInflater)
        viewModel.saveConfiguration()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        binding.clientHomeActivityLogIbtn.setOnClickListener {
            findNavController().navigate(R.id.actionDashboardToLogs)
        }
    }

    private fun setupObservers() {
        viewModel.selectedChildLiveData.observeNonNull(viewLifecycleOwner) { child ->
            with(binding) {
                clientHomeBabyNameTv.apply {
                    if (child.name.isNullOrBlank()) {
                        text = getString(R.string.your_baby_name)
                        setTextColor(getColor(R.color.accent))
                    } else {
                        text = child.name
                        setTextColor(getColor(R.color.white))
                    }
                }

                if (!child.image.isNullOrEmpty()) {
                    GlideApp.with(requireContext())
                        .load(child.image)
                        .apply(RequestOptions.circleCropTransform())
                        .into(clientHomeBabyIv)
                    clientHomeBabyIv.setPadding(NO_PADDING)
                }
            }
        }
        viewModel.selectedChildAvailability.observeNonNull(viewLifecycleOwner) { childAvailable ->
            if (childAvailable) {
                showClientConnected()
            } else {
                showClientDisconnected()
            }
        }
    }

    private fun showClientConnected() {
        with(binding) {
            clientConnectionStatusTv.text = getString(R.string.devices_connected)
            clientConnectionStatusPv.start()
            clientHomeLiveCameraIbtn.setOnClickListener {
                requestMicrophonePermission()
            }
        }
    }

    private fun showClientDisconnected() {
        with(binding) {
            clientConnectionStatusTv.text = getString(R.string.devices_disconnected)
            clientConnectionStatusPv.stop()
            clientHomeLiveCameraIbtn.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.child_not_available),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun requestMicrophonePermission() {
        requestPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_MICROPHONE_PERMISSION
        )
    }

    private fun showRationaleSnackbar() {
        Snackbar.make(
            requireView(),
            getString(R.string.parent_microphone_permission),
            Snackbar.LENGTH_SHORT
        )
            .setAction(getString(R.string.check_again)) { requestMicrophonePermission() }
            .setDuration(BaseTransientBottomBar.LENGTH_LONG)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val recordAudioResult = PermissionUtils.getPermissionsRequestResult(
            requireActivity(),
            REQUEST_MICROPHONE_PERMISSION,
            requestCode,
            grantResults,
            Manifest.permission.RECORD_AUDIO
        )

        when (recordAudioResult) {
            PermissionResult.SHOW_RATIONALE -> showRationaleSnackbar()
            PermissionResult.GRANTED -> findNavController().navigate(R.id.clientLiveCamera)
            PermissionResult.NOT_GRANTED -> findNavController().navigate(R.id.clientLiveCamera)
        }
    }

    companion object {
        private const val REQUEST_MICROPHONE_PERMISSION = 678
        private const val NO_PADDING = 0
    }
}
