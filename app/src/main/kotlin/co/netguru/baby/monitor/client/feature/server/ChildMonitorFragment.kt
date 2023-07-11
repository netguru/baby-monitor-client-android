package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.client.BuildConfig
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.common.YesNoDialog
import co.netguru.baby.monitor.client.common.base.BaseFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.bindService
import co.netguru.baby.monitor.client.common.extensions.daggerParentActivityViewModel
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.databinding.FragmentChildMonitorBinding
import co.netguru.baby.monitor.client.feature.analytics.Screen
import co.netguru.baby.monitor.client.feature.batterylevel.LowBatteryPublishSubject
import co.netguru.baby.monitor.client.feature.batterylevel.LowBatteryReceiver
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdState
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.server.WebRtcService
import co.netguru.baby.monitor.client.feature.debug.DebugModule
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisService
import co.netguru.baby.monitor.client.feature.voiceAnalysis.VoiceAnalysisService.VoiceAnalysisBinder
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@Suppress("TooManyFunctions")
class ChildMonitorFragment : BaseFragment(R.layout.fragment_child_monitor), ServiceConnection {

    private lateinit var binding: FragmentChildMonitorBinding

    private val serverViewModel: ServerViewModel by daggerParentActivityViewModel { serverViewModelProvider }

    private val childMonitorViewModel: ChildMonitorViewModel by daggerParentActivityViewModel { childMonitorViewModelProvider }

    @Inject
    lateinit var serverViewModelProvider: Provider<ServerViewModel>

    @Inject
    lateinit var childMonitorViewModelProvider: Provider<ChildMonitorViewModel>

    override val screen: Screen = Screen.CHILD_MONITOR

    @Inject
    internal lateinit var debugModule: DebugModule

    private var voiceAnalysisServiceBinder: VoiceAnalysisBinder? = null
    private var webRtcServiceBinder: WebRtcService.Binder? = null

    private val compositeDisposable = CompositeDisposable()

    @Inject
    internal lateinit var lowBatteryReceiver: LowBatteryReceiver

    @Inject
    lateinit var lowBatteryPublishSubject: LowBatteryPublishSubject
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverViewModel.saveConfiguration()
        lowBatteryReceiver.register(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChildMonitorBinding.inflate(layoutInflater)
        handleLowBattery()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
        webRtcServiceBinder?.addSurfaceView(binding.surfaceView)
    }

    override fun onResume() {
        super.onResume()
        registerNsdService()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            bindServices()
        }
    }

    override fun onPause() {
        serverViewModel.unregisterNsdService()
        serverViewModel.toggleVideoPreview(false)
        super.onPause()
    }

    override fun onDestroyView() {
        with(binding) {
            surfaceView.release()
            debugView.clearDebugStateObservable()
        }
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    override fun onDestroy() {
        requireContext().unbindService(this)
        requireContext().unregisterReceiver(lowBatteryReceiver)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(Companion.permissions)) {
            bindServices()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("Service Disconnected: $name")
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        when (service) {
            is WebRtcService.Binder ->
                handleWebRtcBinder(service)

            is VoiceAnalysisBinder -> {
                Timber.i("VoiceAnalysisService service connected")
                voiceAnalysisServiceBinder = service
            }
        }
    }


    private fun handleLowBattery() {
        lowBatteryPublishSubject.publishSubject.subscribe {
            childMonitorViewModel.notifyLowBattery(
                title = getString(R.string.notification_low_battery_title),
                text = getString(R.string.notification_low_battery_text)
            )
        }.apply {
            compositeDisposable.add(this)
        }
    }

    private fun setupView() {
        with(binding) {
            nightModeToggleBtn.setOnClickListener {
                childMonitorViewModel.switchNightMode()
            }
            settingsIbtn.setOnClickListener {
                serverViewModel.toggleDrawer(true)
            }
            videoPreviewButton.setOnClickListener { serverViewModel.toggleVideoPreview(true) }
            debugView.apply {
                setDebugStateObservable(debugModule.debugStateObservable())
                isVisible = BuildConfig.DEBUG
            }
        }
    }

    private fun setupObservers() {
        serverViewModelObservers()
        childMonitorObservables()
    }

    private fun childMonitorObservables() {
        childMonitorViewModel.nightModeStatus.observe(
            viewLifecycleOwner,
            Observer { isNightModeEnabled ->
                binding.nightModeGroup.isVisible = isNightModeEnabled
            })
    }

    private fun dismissPairingDialog() {
        currentPairingDialog()?.dismiss()
    }

    private fun currentPairingDialog() =
        childFragmentManager.findFragmentByTag(PAIRING_CODE_DIALOG_TAG) as? DialogFragment

    private fun showPairingDialog(pairingCode: String) {
        dismissPairingDialog()
        YesNoDialog.newInstance(
            R.string.pairing_dialog_title,
            requireContext().getString(R.string.pairing_dialog_message, pairingCode),
            R.string.accept,
            R.string.decline
        ).show(childFragmentManager, PAIRING_CODE_DIALOG_TAG)
    }

    private fun serverViewModelObservers() {
        with(binding) {
            serverViewModel.babyNameStatus.observeNonNull(viewLifecycleOwner) { name ->
                babyName.text = name
                babyName.visibility =
                    if (name.isBlank()) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }
            serverViewModel.previewingVideo.observeNonNull(viewLifecycleOwner) { previewing ->
                if (previewing) {
                    showVideoPreview()
                } else {
                    hideVideoPreview()
                }
            }

            serverViewModel.timer.observe(viewLifecycleOwner, Observer { secondsLeft ->
                timer.text = if (secondsLeft != null && secondsLeft < VIDEO_PREVIEW_MAX_TIME) {
                    getString(
                        R.string.message_disabling_video_preview_soon,
                        "0:%02d".format(secondsLeft)
                    )
                } else {
                    ""
                }
            })


            serverViewModel.rtcConnectionStatus.observeNonNull(viewLifecycleOwner) { connectionState ->
                when (connectionState) {
                    RtcConnectionState.ConnectionOffer -> voiceAnalysisServiceBinder?.stopRecording()
                    RtcConnectionState.Disconnected,
                    RtcConnectionState.Error -> voiceAnalysisServiceBinder?.startRecording()

                    else -> Unit
                }
            }
            serverViewModel.cameraState.observe(viewLifecycleOwner, Observer { cameraState ->
                webRtcServiceBinder?.enableCamera(
                    cameraState.previewEnabled ||
                            cameraState.streamingEnabled
                )
            })
            serverViewModel.pulsatingViewStatus.observe(viewLifecycleOwner, Observer { status ->
                Timber.d("Client status: $status.")
                when (status) {
                    ClientConnectionStatus.CLIENT_CONNECTED ->
                        pulsatingView.start()

                    ClientConnectionStatus.EMPTY ->
                        pulsatingView.stop()
                }
            })
        }
        serverViewModel.pairingCodeLiveData.observe(viewLifecycleOwner, Observer { pairingCode ->
            if (pairingCode.isNotEmpty()) {
                showPairingDialog(pairingCode)
            } else {
                dismissPairingDialog()
            }
        })

        serverViewModel.voiceAnalysisOptionLiveData.observe(
            viewLifecycleOwner,
            Observer { voiceAnalysisOption ->
                voiceAnalysisServiceBinder?.setVoiceAnalysisOption(voiceAnalysisOption)
            })
        serverViewModel.noiseLevelLiveData.observe(
            viewLifecycleOwner,
            Observer { noiseLevel ->
                voiceAnalysisServiceBinder?.setNoiseDetectionLevel(noiseLevel)
            }
        )
    }

    private fun showVideoPreview() {
        Timber.i("showVideoPreview")
        with(binding) {
            videoPreviewGroup.isVisible = true
            videoPreviewTogglingGroup.isVisible = false
            serverViewModel.resetTimer()
            surfaceView.disableFpsReduction()
        }
    }

    private fun hideVideoPreview() {
        Timber.i("hideVideoPreview")
        with(binding) {
            videoPreviewGroup.isVisible = false
            videoPreviewTogglingGroup.isVisible = true
            surfaceView.pauseVideo()
        }
    }

    private fun registerNsdService() {
        serverViewModel.registerNsdService()
        serverViewModel.nsdState.observe(viewLifecycleOwner, Observer {
            if (it is NsdState.Error) showSnackbarMessage(R.string.nsd_service_registration_failed)
        })
    }

    private fun bindServices() {
        bindService(
            VoiceAnalysisService::class.java,
            this,
            Service.BIND_AUTO_CREATE
        )
        requireContext().run {
            bindService(
                Intent(this, WebRtcService::class.java),
                this@ChildMonitorFragment,
                Service.BIND_AUTO_CREATE
            )
        }
    }

    private fun handleWebRtcBinder(webRtcServiceBinder: WebRtcService.Binder) {
        serverViewModel.handleRtcServerConnectionState(webRtcServiceBinder)
        this.webRtcServiceBinder = webRtcServiceBinder
        serverViewModel.toggleVideoPreview(true)
        webRtcServiceBinder.addSurfaceView(binding.surfaceView)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125
        private const val VIDEO_PREVIEW_MAX_TIME = 60
        private const val PAIRING_CODE_DIALOG_TAG = "PAIRING_DIALOG_TAG"

        private val permissions = arrayOf(
            RECORD_AUDIO, CAMERA
        )
    }
}
