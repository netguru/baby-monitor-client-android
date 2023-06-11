package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.App
import co.netguru.baby.monitor.client.common.PermissionUtils
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.daggerViewModel
import co.netguru.baby.monitor.client.common.extensions.scaleAnimation
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.databinding.FragmentClientLiveCameraBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.babynotification.BabyEventActionIntentService
import co.netguru.baby.monitor.client.feature.client.home.BackButtonState
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.communication.webrtc.ConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import timber.log.Timber
import java.net.URI
import javax.inject.Inject
import javax.inject.Provider

class ClientLiveCameraFragment : BaseFragment(R.layout.fragment_client_live_camera) {
    override val screen: Screen = Screen.CLIENT_LIVE_CAMERA
    private lateinit var binding : FragmentClientLiveCameraBinding

    private val viewModel by daggerViewModel { viewModelProvider }

    private val fragmentViewModel by daggerViewModel { fragmentViewModelProvider }

    @Inject
    lateinit var viewModelProvider: Provider<ClientHomeViewModel>

    @Inject
    lateinit var fragmentViewModelProvider: Provider<ClientLiveCameraFragmentViewModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as App).appComponent.inject(this)
        binding = FragmentClientLiveCameraBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setBackButtonState(
            BackButtonState(
                true,
                shouldShowSnoozeDialogOnBack()
            )
        )
        setupPushToSpeakButton()
        setupObservers()
    }

    private fun setupPushToSpeakButton() {
        if (PermissionUtils.arePermissionsGranted(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            )
        ) {
            enablePushToSpeakButton()
        } else {
            binding.pushToSpeakButton.isVisible = false
        }
    }

    private fun enablePushToSpeakButton() {
        with(binding) {
            pushToSpeakButton.isVisible = true
            pushToSpeakButton.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onPushToSpeakButtonPressed()
                } else if (event.action == MotionEvent.ACTION_UP) {
                    onPushToSpeakButtonRelease()
                }
                true
            }
        }
    }

    private fun onPushToSpeakButtonRelease() {
        fragmentViewModel.pushToSpeak(false)
        binding.pushToSpeakButton.scaleAnimation(
            false,
            PRESSED_SCALE,
            NORMAL_SCALE,
            ANIMATION_DURATION
        )
    }

    private fun onPushToSpeakButtonPressed() {
        fragmentViewModel.pushToSpeak(true)
        binding.pushToSpeakButton.scaleAnimation(
            true,
            PRESSED_SCALE,
            NORMAL_SCALE,
            ANIMATION_DURATION
        )
    }

    private fun setupObservers() {
        viewModel.selectedChildAvailability.observe(
            viewLifecycleOwner,
            Observer { it?.let(::onAvailabilityChange) })
        fragmentViewModel.streamState.observe(viewLifecycleOwner, Observer {
            handleStreamStateChange(it)
        })
    }

    override fun onStart() {
        super.onStart()
        maybeStartCall()
    }

    override fun onStop() {
        super.onStop()
        fragmentViewModel.endCall()
    }

    private fun shouldShowSnoozeDialogOnBack() =
        arguments?.getBoolean(BabyEventActionIntentService.SHOULD_SHOW_SNOOZE_DIALOG) == true

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setBackButtonState(
            BackButtonState(
                shouldBeVisible = false,
                shouldShowSnoozeDialog = false
            )
        )
    }

    private fun onAvailabilityChange(connectionAvailable: Boolean) {
        Timber.d("onAvailabilityChange($connectionAvailable)")
        if (connectionAvailable) {
            maybeStartCall()
        } else {
            fragmentViewModel.endCall()
        }
    }

    private fun maybeStartCall() {
        if (!fragmentViewModel.callInProgress.get()) startCall(
            viewModel.rxWebSocketClient,
            PermissionUtils.arePermissionsGranted(
                requireContext(),
                android.Manifest.permission.RECORD_AUDIO
            )
        )
    }

    private fun startCall(rxWebSocketClient: RxWebSocketClient, hasRecordAudioPermission: Boolean) {
        val serverUri = URI.create(viewModel.selectedChildLiveData.value?.address ?: return)
        fragmentViewModel.startCall(
            requireActivity().applicationContext,
            binding.liveCameraRemoteRenderer,
            serverUri,
            rxWebSocketClient,
            hasRecordAudioPermission
        )
    }

    private fun handleStreamStateChange(streamState: StreamState) {
        when ((streamState as? ConnectionState)?.connectionState) {
            RtcConnectionState.Connected,
            RtcConnectionState.Completed -> binding.streamProgressBar.isVisible = false
            RtcConnectionState.Checking -> binding.streamProgressBar.isVisible = true
            RtcConnectionState.Error -> handleBabyDeviceSdpError()
            else -> Unit
        }
    }

    private fun handleBabyDeviceSdpError() {
        showSnackbarMessage(R.string.stream_error)
        requireActivity().onBackPressed()
    }

    companion object {
        private const val NORMAL_SCALE = 1.0f
        private const val PRESSED_SCALE = 2.5f
        private const val ANIMATION_DURATION = 500L
    }
}
