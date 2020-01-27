package co.netguru.baby.monitor.client.feature.client.home.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import co.netguru.baby.monitor.client.common.PermissionResult
import co.netguru.baby.monitor.client.common.PermissionUtils
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.getColor
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_client_dashboard.*
import javax.inject.Inject

class ClientDashboardFragment : BaseFragment() {
    override val layoutResource = R.layout.fragment_client_dashboard
    override val screen: Screen = Screen.CLIENT_DASHBOARD

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.saveConfiguration()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        clientHomeActivityLogIbtn.setOnClickListener {
            findNavController().navigate(R.id.actionDashboardToLogs)
        }
    }

    private fun setupObservers() {
        viewModel.selectedChild.observeNonNull(viewLifecycleOwner) { child ->
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
        viewModel.selectedChildAvailability.observeNonNull(viewLifecycleOwner) { childAvailable ->
            if (childAvailable) {
                showClientConnected()
            } else {
                showClientDisconnected()
            }
        }
    }

    private fun showClientConnected() {
        clientConnectionStatusTv.text = getString(R.string.monitoring_enabled)
        clientConnectionStatusPv.start()
        clientHomeLiveCameraIbtn.setOnClickListener {
            requestMicrophonePermission()
        }
    }

    private fun showClientDisconnected() {
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

    private fun requestMicrophonePermission() {
        requestPermissions(
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
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
            android.Manifest.permission.RECORD_AUDIO
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
