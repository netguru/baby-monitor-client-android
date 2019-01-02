package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.app.Service
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.feature.common.extensions.bindService
import co.netguru.baby.monitor.client.feature.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import dagger.android.support.DaggerFragment
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_server.*
import timber.log.Timber
import javax.inject.Inject

//TODO Should be refactored
class ServerFragment : DaggerFragment(), ServiceConnection {

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ServerViewModel::class.java]
    }
    private var rtcServiceBinder: WebRtcService.MainBinder? = null
    private var machineLearningServiceBinder: MachineLearningService.MainBinder? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_server, container, false)

    override fun onResume() {
        super.onResume()
        viewModel.registerNsdService()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            bindServices()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterNsdService()
        requireContext().unbindService(this)
    }

    override fun onDestroy() {
        rtcServiceBinder?.cleanup()
        machineLearningServiceBinder?.cleanup()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(Companion.permissions)) {
            bindServices()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("Service Disconnected: $name")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        when (service) {
            is WebRtcService.MainBinder -> {
                Timber.i("WebRtcService service connected")
                rtcServiceBinder = service
                service.createReceiver(
                        surfaceView,
                        this@ServerFragment::handleCallStateChange
                )
            }
            is MachineLearningService.MainBinder -> {
                Timber.i("MachineLearningService service connected")
                machineLearningServiceBinder = service
                service.setOnCryingBabyDetectedListener {
                    rtcServiceBinder?.handleBabyCrying()
                }
            }
        }
    }

    private fun handleCallStateChange(state: CallState) {
        when (state) {
            CallState.CONNECTING -> {
                machineLearningServiceBinder?.stopRecording()
            }
            CallState.ENDED -> {
                rtcServiceBinder?.let(this::endCall)
            }
        }
    }

    private fun endCall(binder: WebRtcService.MainBinder) {
        binder.hangUpReceiver()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            machineLearningServiceBinder?.startRecording()
                        },
                        onError = Timber::e
                )
    }

    private fun bindServices() {
        bindService(
                WebRtcService::class.java,
                this,
                Service.BIND_AUTO_CREATE
        )
        bindService(
                MachineLearningService::class.java,
                this,
                Service.BIND_AUTO_CREATE
        )
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                RECORD_AUDIO, CAMERA
        )
    }
}
