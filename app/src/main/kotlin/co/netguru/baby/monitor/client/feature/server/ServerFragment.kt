package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.*
import android.app.Service
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.feature.common.DefaultServiceConnection
import co.netguru.baby.monitor.client.feature.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.feature.common.extensions.bindService
import co.netguru.baby.monitor.client.feature.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcReceiver
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
        super.onDestroy()
        viewModel.hangUp()?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeBy(
                        onComplete = { Timber.e("disconnected") },
                        onError = Timber::e
                )
        rtcServiceBinder?.cleanup()
        machineLearningServiceBinder?.cleanup()
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
        machineLearningServiceBinder?.startRecording()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        when(service) {
            is WebRtcService.MainBinder -> {
                Timber.i("WebRtcService service connected")
                rtcServiceBinder = service
                rtcServiceBinder?.callChangeNotifier = { call ->
                    machineLearningServiceBinder?.stopRecording()
                    viewModel.accept(
                            call as RtcReceiver,
                            requireContext(),
                            this@ServerFragment::handleCallStateChange
                    )
                }
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
            CallState.ENDED -> {
                rtcServiceBinder?.let(this::endCall)
            }
        }
    }

    private fun endCall(binder: WebRtcService.MainBinder) {
        binder.hangUp()
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
